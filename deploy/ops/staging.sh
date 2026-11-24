#!/usr/bin/env bash
set -euo pipefail
umask 077

script_dir=$(cd -- "$(dirname -- "$0")" && pwd)
stage_dir="$HOME/bmpn-staging"

[[ "${BACKEND_IMAGE:-}" =~ ^ghcr\.io/andrxco/bmpn-deploy-backend@sha256:[a-f0-9]{64}$ ]]
[[ "${FRONT_IMAGE:-}" =~ ^ghcr\.io/andrxco/bmpn-deploy-front@sha256:[a-f0-9]{64}$ ]]

mkdir -p -- "$stage_dir"
cd -- "$stage_dir"
exec 9>.deploy.lock
flock -w 180 9

if [[ ! -f .env ]]; then
  env_file=$(mktemp .env.XXXXXX)
  {
    printf 'POSTGRES_PASSWORD=%s\n' "$(openssl rand -hex 32)"
    printf 'BMPN_JWT_SECRET=%s\n' "$(openssl rand -hex 32)"
    printf 'DATOS_INICIALES=true\n'
    printf 'DATOS_INICIALES_CLAVE=%s\n' "$(openssl rand -hex 24)"
    printf 'BMPN_REFRESH_COOKIE_SECURE=false\n'
    printf 'CORS_ORIGENES=http://127.0.0.1:18080,http://localhost:18080\n'
    printf 'HTTP_PORT=18080\n'
  } > "$env_file"
  mv -- "$env_file" .env
fi

docker_auth=$(mktemp -d)
trap 'rm -f -- "$docker_auth/config.json"; rmdir -- "$docker_auth"' EXIT
export DOCKER_CONFIG="$docker_auth"
printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u "$GITHUB_ACTOR" --password-stdin
unset GHCR_TOKEN

install -m 600 -- "$script_dir/compose.yaml" compose.yaml
compose=(docker compose -p bmpn-staging --env-file .env -f compose.yaml)
"${compose[@]}" pull
"${compose[@]}" up -d --wait --wait-timeout 360

origin=http://127.0.0.1:18080
curl -fsS --max-time 10 "$origin/healthz"
api_status=$(curl -sS --max-time 10 -o /dev/null -w '%{http_code}' "$origin/api/empresas")
if [[ "$api_status" != 401 && "$api_status" != 403 ]]; then
  echo "Expected Spring Security to reject the unauthenticated API request, got $api_status" >&2
  exit 1
fi

printf 'BACKEND_IMAGE=%s\nFRONT_IMAGE=%s\n' "$BACKEND_IMAGE" "$FRONT_IMAGE" > images.env
"${compose[@]}" ps
{
  echo '### Imágenes verificadas en staging'
  printf -- '- Backend: `%s`\n- Frontend: `%s`\n' "$BACKEND_IMAGE" "$FRONT_IMAGE"
} >> "$GITHUB_STEP_SUMMARY"
