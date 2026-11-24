#!/usr/bin/env bash
set -euo pipefail
umask 077

script_dir=$(cd -- "$(dirname -- "$0")" && pwd)
production_dir="$HOME/bmpn-production"
[[ "${BACKEND_IMAGE:-}" =~ ^ghcr\.io/andrxco/bmpn-deploy-backend@sha256:[a-f0-9]{64}$ ]]
[[ "${FRONT_IMAGE:-}" =~ ^ghcr\.io/andrxco/bmpn-deploy-front@sha256:[a-f0-9]{64}$ ]]

cd -- "$production_dir"
if [[ ! -O .env || "$(stat -c %a .env)" != 600 ]]; then
  echo 'El servidor debe tener un .env propio con permisos 600.' >&2
  exit 1
fi
exec 9>.deploy.lock
flock -w 180 9

docker_auth=$(mktemp -d)
pending_backup=
cleanup() {
  rm -f -- "$docker_auth/config.json"
  rmdir -- "$docker_auth"
  if [[ -n "$pending_backup" ]]; then rm -f -- "$pending_backup"; fi
}
trap cleanup EXIT
export DOCKER_CONFIG="$docker_auth"

compose=(docker compose -p bmpn-production --env-file .env -f "$script_dir/compose.yaml")
origin=$("${compose[@]}" config --format json | python3 -c '
import json, sys
env = json.load(sys.stdin)["services"]["app"]["environment"]
origins = env["CORS_ORIGENES"].split(",")
assert env["BMPN_REFRESH_COOKIE_SECURE"] == "true", "La cookie de producción debe ser segura"
assert env["DATOS_INICIALES"] == "false", "Producción requiere DATOS_INICIALES=false"
assert all(origin.startswith("https://") for origin in origins), "Producción requiere un origen HTTPS"
print(origins[0])
')

printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u "$GITHUB_ACTOR" --password-stdin
unset GHCR_TOKEN
"${compose[@]}" pull app front
"${compose[@]}" up -d --no-recreate --wait --wait-timeout 180 postgres

mkdir -p -- backups
pending_backup=$(mktemp backups/.predeploy.XXXXXX)
"${compose[@]}" exec -T postgres sh -c 'pg_dump -Fc -U "$POSTGRES_USER" "$POSTGRES_DB"' > "$pending_backup"
"${compose[@]}" exec -T postgres pg_restore --list < "$pending_backup" > /dev/null
backup_file="backups/predeploy-$(date -u +%Y%m%dT%H%M%SZ)-$GITHUB_RUN_ID-$GITHUB_RUN_ATTEMPT.dump"
mv -- "$pending_backup" "$backup_file"
pending_backup=
printf 'Respaldo verificado: %s\n' "$backup_file"

"${compose[@]}" up -d --no-deps --wait --wait-timeout 360 app front
curl -fsS --max-time 20 "$origin/healthz"
api_status=$(curl -sS --max-time 20 -o /dev/null -w '%{http_code}' "$origin/api/empresas")
[[ "$api_status" == 401 || "$api_status" == 403 ]]
printf 'BACKEND_IMAGE=%s\nFRONT_IMAGE=%s\n' "$BACKEND_IMAGE" "$FRONT_IMAGE" > images.env
"${compose[@]}" ps
