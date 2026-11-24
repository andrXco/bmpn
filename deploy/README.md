# Despliegue de BPMN

El backend `andrXco/bmpn` y el frontend `andrXco/bmpn-react` mantienen el
despliegue en su rama `deploy`. GitHub Actions prueba cada revisión y publica la
imagen Docker que pasó los controles como una prepublicación `staging-<SHA>` de
su propio repositorio. Los archivos publicados contienen la imagen y su suma
SHA256. Las credenciales y el `.env` viven en Ubuntu.

## Staging en Ubuntu

Un timer de systemd de usuario consulta la punta de `deploy` en ambos repos cada
tres minutos. Si hay una imagen verificada para cada SHA, descarga y comprueba
los archivos por HTTPS y actualiza `~/bmpn-staging` con Docker Compose. El stack
usa su propio volumen de PostgreSQL y publica el frontend en `127.0.0.1:18080`.
Si una de las imágenes aún está en CI, se conserva la versión que está sirviendo.
`images.env` registra las dos imágenes aplicadas después del control de salud.

Instalación inicial del timer, desde el checkout del backend en Ubuntu:

```bash
cd ~/bmpn
git fetch origin deploy
git switch deploy
git merge --ff-only origin/deploy
install -d ~/.config/systemd/user
install -m 644 deploy/ops/bmpn-staging.{service,timer} ~/.config/systemd/user/
systemctl --user daemon-reload
systemctl --user enable --now bmpn-staging.timer
loginctl enable-linger "$USER"
```

Para ejecutar la actualización de inmediato o consultar su resultado:

```bash
systemctl --user start bmpn-staging.service
journalctl --user -u bmpn-staging.service -n 30 --no-pager
```

Desde el computador con acceso SSH a la VM:

```powershell
ssh -N -L 18080:127.0.0.1:18080 estudiante@10.43.99.131
```

Abre `http://127.0.0.1:18080` mientras el túnel esté activo.

## Producción

Prepara una VM aparte con Docker Compose, DNS y proxy HTTPS, conservando el
backend y PostgreSQL en la red privada de Compose. Crea `~/bmpn-production/.env`
con permisos `600`, credenciales propias, `BMPN_REFRESH_COOKIE_SECURE=true`,
`DATOS_INICIALES=false` y el origen HTTPS real en `CORS_ORIGENES`.

Con la rama `deploy` del backend en esa VM, promueve un par de SHA ya probado
en staging con `bash ~/bmpn/deploy/ops/production.sh SHA_BACKEND SHA_FRONTEND`.
El script descarga las mismas imágenes y valida el respaldo de PostgreSQL
antes de actualizar. Flyway aplica las migraciones al arrancar. Para una
migración incompatible, prueba la restauración del respaldo con las versiones
anteriores. La réplica física se configura por separado.
