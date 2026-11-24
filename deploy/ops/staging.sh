#!/usr/bin/env bash
set -euo pipefail
umask 077

script_dir=$(cd -- "$(dirname -- "$0")" && pwd)
source "$script_dir/release-images.sh"

stage_dir="$HOME/bmpn-staging"
mkdir -p -- "$stage_dir"
cd -- "$stage_dir"
exec 9>.deploy.lock
flock -n 9 || exit 0

backend_sha=$(deploy_head bmpn)
front_sha=$(deploy_head bmpn-react)
if [[ -f images.env ]] &&
  grep -Fxq "BACKEND_IMAGE=bmpn-release-backend:$backend_sha" images.env &&
  grep -Fxq "FRONT_IMAGE=bmpn-release-front:$front_sha" images.env; then
  exit 0
fi

work_dir=$(mktemp -d "$stage_dir/.release.XXXXXX")
trap 'rm -rf -- "$work_dir"' EXIT
if prepare_release_pair "$backend_sha" "$front_sha"; then
  echo "Imágenes verificadas para $backend_sha y $front_sha"
else
  result=$?
  if [[ "$result" == 4 ]]; then
    echo 'Esperando que GitHub Actions publique ambas imágenes de deploy.'
    exit 0
  fi
  exit "$result"
fi

if [[ ! -f .env ]]; then
  {
    printf 'POSTGRES_PASSWORD=%s\n' "$(openssl rand -hex 32)"
    printf 'BMPN_JWT_SECRET=%s\n' "$(openssl rand -hex 32)"
    printf 'DATOS_INICIALES=true\n'
    printf 'DATOS_INICIALES_CLAVE=%s\n' "$(openssl rand -hex 24)"
    printf 'BMPN_REFRESH_COOKIE_SECURE=false\n'
    printf 'CORS_ORIGENES=http://127.0.0.1:18080,http://localhost:18080\n'
    printf 'HTTP_PORT=18080\n'
  } > "$work_dir/.env"
  mv -- "$work_dir/.env" .env
fi

compose=(docker compose -p bmpn-staging --project-directory "$stage_dir" --env-file .env -f "$work_dir/compose.yaml")
"${compose[@]}" up -d --no-recreate --wait --wait-timeout 180 postgres
"${compose[@]}" up -d --no-deps --pull never --wait --wait-timeout 240 app
"${compose[@]}" up -d --no-deps --pull never --force-recreate --wait --wait-timeout 90 front
verify_application http://127.0.0.1:18080
save_release_state
"${compose[@]}" ps
