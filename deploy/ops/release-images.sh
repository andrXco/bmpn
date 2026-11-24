#!/usr/bin/env bash

deploy_head() {
  git ls-remote --exit-code "https://github.com/andrxco/$1.git" refs/heads/deploy | cut -f1
}

release_url() {
  local repo=$1 sha=$2
  [[ "$repo" == bmpn || "$repo" == bmpn-react ]] || return 1
  [[ "$sha" =~ ^[a-f0-9]{40}$ ]] || return 1
  printf 'https://github.com/andrxco/%s/releases/download/staging-%s' "$repo" "$sha"
}

fetch_checksum() {
  local url=$1 output=$2 status
  status=$(curl --proto '=https' --proto-redir '=https' --silent --show-error \
    --location --connect-timeout 10 --max-time 60 --output "$output" \
    --write-out '%{http_code}' "$url/image.tar.gz.sha256") || return 1
  case "$status" in
    200) return 0 ;;
    404) return 4 ;;
    *) printf 'GitHub devolvió HTTP %s al pedir la suma de verificación.\n' "$status" >&2; return 1 ;;
  esac
}

load_release_image() {
  local url=$1 image=$2 checksum=$3 archive hash filename
  if docker image inspect "$image" >/dev/null 2>&1; then return 0; fi
  read -r hash filename < "$checksum" || return 1
  [[ "$hash" =~ ^[a-f0-9]{64}$ && "$filename" == image.tar.gz ]] || return 1
  archive=$(mktemp "$work_dir/image.XXXXXX") || return 1
  curl --proto '=https' --proto-redir '=https' --fail --silent --show-error \
    --location --retry 2 --connect-timeout 10 --max-time 300 \
    --output "$archive" "$url/image.tar.gz" || return 1
  printf '%s  %s\n' "$hash" "$archive" | sha256sum --check --status || return 1
  docker load --input "$archive" || return 1
  docker image inspect "$image" >/dev/null || return 1
  rm -- "$archive"
}

prepare_release_pair() {
  local back_url front_url release url rc checkout_dir
  back_url=$(release_url bmpn "$1") || return 1
  front_url=$(release_url bmpn-react "$2") || return 1
  for release in backend front; do
    if [[ "$release" == backend ]]; then url=$back_url; else url=$front_url; fi
    if fetch_checksum "$url" "$work_dir/$release.sha256"; then continue; else rc=$?; fi
    return "$rc"
  done

  export BACKEND_IMAGE="bmpn-release-backend:$1"
  export FRONT_IMAGE="bmpn-release-front:$2"
  load_release_image "$back_url" "$BACKEND_IMAGE" "$work_dir/backend.sha256" || return 1
  load_release_image "$front_url" "$FRONT_IMAGE" "$work_dir/front.sha256" || return 1
  checkout_dir=$(cd -- "$script_dir/../.." && pwd) || return 1
  if ! git -C "$checkout_dir" cat-file -e "$1^{commit}" 2>/dev/null; then
    git -C "$checkout_dir" fetch --no-tags https://github.com/andrXco/bmpn.git "$1" || return 1
  fi
  git -C "$checkout_dir" show "$1:deploy/compose.yaml" > "$work_dir/compose.yaml"
}

verify_application() {
  local origin=$1 status
  curl -fsS --max-time 20 "$origin/healthz"
  status=$(curl -sS --max-time 20 -o /dev/null -w '%{http_code}' "$origin/api/empresas")
  if [[ "$status" != 401 && "$status" != 403 ]]; then
    printf 'Respuesta inesperada de la API: HTTP %s\n' "$status" >&2
    return 1
  fi
}

save_release_state() {
  install -m 600 -- "$work_dir/compose.yaml" compose.yaml
  printf 'BACKEND_IMAGE=%s\nFRONT_IMAGE=%s\n' "$BACKEND_IMAGE" "$FRONT_IMAGE" > "$work_dir/images.env"
  mv -f -- "$work_dir/images.env" images.env
}
