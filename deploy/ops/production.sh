#!/usr/bin/env bash
set -euo pipefail
umask 077

if [[ $# != 2 ]]; then
  printf 'Uso: bash %s SHA_BACKEND SHA_FRONTEND\n' "$0" >&2
  exit 2
fi

script_dir=$(cd -- "$(dirname -- "$0")" && pwd)
source "$script_dir/release-images.sh"
production_dir="$HOME/bmpn-production"
cd -- "$production_dir"
if [[ ! -O .env || "$(stat -c %a .env)" != 600 ]]; then
  echo 'El servidor debe tener un .env propio con permisos 600.' >&2
  exit 1
fi
exec 9>.deploy.lock
flock -w 180 9

work_dir=$(mktemp -d "$production_dir/.release.XXXXXX")
trap 'rm -rf -- "$work_dir"' EXIT
if ! prepare_release_pair "$1" "$2"; then
  echo 'Comprueba que los dos SHA correspondan a imágenes de staging publicadas.' >&2
  exit 1
fi

compose=(docker compose -p bmpn-production --project-directory "$production_dir" --env-file .env -f "$work_dir/compose.yaml")
origin=$("${compose[@]}" config --format json | python3 -c '
import json, sys
env = json.load(sys.stdin)["services"]["app"]["environment"]
origins = [origin.strip() for origin in env["CORS_ORIGENES"].split(",")]
assert env["BMPN_REFRESH_COOKIE_SECURE"] == "true", "La cookie de producción debe ser segura"
assert env["DATOS_INICIALES"] == "false", "Producción requiere DATOS_INICIALES=false"
assert all(origin.startswith("https://") for origin in origins), "Producción requiere un origen HTTPS"
print(origins[0])
')

"${compose[@]}" up -d --no-recreate --wait --wait-timeout 180 postgres
mkdir -p -- backups
pending_backup="$work_dir/predeploy.dump"
"${compose[@]}" exec -T postgres sh -c 'pg_dump -Fc -U "$POSTGRES_USER" "$POSTGRES_DB"' > "$pending_backup"
"${compose[@]}" exec -T postgres pg_restore --list < "$pending_backup" > /dev/null
backup_file="backups/predeploy-$(date -u +%Y%m%dT%H%M%SZ).dump"
mv -- "$pending_backup" "$backup_file"
printf 'Respaldo verificado: %s\n' "$backup_file"

"${compose[@]}" up -d --no-deps --pull never --wait --wait-timeout 240 app
"${compose[@]}" up -d --no-deps --pull never --force-recreate --wait --wait-timeout 90 front
verify_application "$origin"
save_release_state
"${compose[@]}" ps
