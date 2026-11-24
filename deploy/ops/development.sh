#!/usr/bin/env bash
set -euo pipefail
umask 077

script_dir=$(cd -- "$(dirname -- "$0")" && pwd)
backend_repo=$(cd -- "$script_dir/../.." && pwd)
front_repo="$HOME/bmpn-react"
dev_dir="$HOME/bmpn-development"
source "$script_dir/release-images.sh"

mkdir -p -- "$dev_dir"
cd -- "$dev_dir"
exec 9>.deploy.lock
flock -n 9 || exit 0

development_head() {
  git -C "$1" fetch -q --no-tags origin refs/heads/development:refs/remotes/origin/development || return 1
  git -C "$1" rev-parse refs/remotes/origin/development
}

backend_sha=$(development_head "$backend_repo")
front_sha=$(development_head "$front_repo")
export BACKEND_IMAGE="bmpn-development-backend:$backend_sha"
export FRONT_IMAGE="bmpn-development-front:$front_sha"

if [[ -f images.env ]] &&
  grep -Fxq "BACKEND_IMAGE=$BACKEND_IMAGE" images.env &&
  grep -Fxq "FRONT_IMAGE=$FRONT_IMAGE" images.env; then
  exit 0
fi

work_dir=$(mktemp -d "$dev_dir/.build.XXXXXX")
trap 'rm -rf -- "$work_dir"' EXIT
mkdir -- "$work_dir/backend" "$work_dir/front"

if ! docker image inspect "$BACKEND_IMAGE" >/dev/null 2>&1; then
  git -C "$backend_repo" archive "$backend_sha" | tar -x -C "$work_dir/backend"
  git -C "$backend_repo" show origin/deploy:Dockerfile > "$work_dir/backend/Dockerfile"
  docker build --label "org.opencontainers.image.revision=$backend_sha" -t "$BACKEND_IMAGE" "$work_dir/backend"
fi
if ! docker image inspect "$FRONT_IMAGE" >/dev/null 2>&1; then
  git -C "$front_repo" archive "$front_sha" | tar -x -C "$work_dir/front"
  git -C "$front_repo" show origin/deploy:Dockerfile > "$work_dir/front/Dockerfile"
  git -C "$front_repo" show origin/deploy:nginx.conf > "$work_dir/front/nginx.conf"
  docker build --build-arg ANGULAR_CONFIGURATION=development \
    --label "org.opencontainers.image.revision=$front_sha" -t "$FRONT_IMAGE" "$work_dir/front"
fi

if [[ ! -f .env ]]; then
  {
    printf 'POSTGRES_PASSWORD=%s\n' "$(openssl rand -hex 32)"
    printf 'BMPN_JWT_SECRET=%s\n' "$(openssl rand -hex 32)"
    printf 'DATOS_INICIALES=true\n'
    printf 'DATOS_INICIALES_CLAVE=%s\n' "$(openssl rand -hex 24)"
    printf 'BMPN_REFRESH_COOKIE_SECURE=false\n'
    printf 'CORS_ORIGENES=http://127.0.0.1:18081,http://localhost:18081\n'
    printf 'HTTP_PORT=18081\n'
  } > "$work_dir/.env"
  mv -- "$work_dir/.env" .env
fi

install -m 600 -- "$script_dir/../compose.yaml" "$work_dir/compose.yaml"
compose=(docker compose -p bmpn-development --project-directory "$dev_dir" --env-file .env -f "$work_dir/compose.yaml")
"${compose[@]}" up -d --no-recreate --wait --wait-timeout 180 postgres
"${compose[@]}" up -d --no-deps --pull never --wait --wait-timeout 240 app
"${compose[@]}" up -d --no-deps --pull never --force-recreate --wait --wait-timeout 90 front
verify_application http://127.0.0.1:18081
save_release_state
"${compose[@]}" ps
