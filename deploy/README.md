# Despliegue de BPMN

El Compose de esta carpeta une Angular, Spring Boot y PostgreSQL. El Compose de
la raíz se usa para desarrollo local. Las pruebas de los dos repos se ejecutan
en los runners hospedados de GitHub.

## Staging en Ubuntu

El repositorio privado [bmpn-deploy](https://github.com/andrXco/bmpn-deploy)
contiene la copia activa de `ops/` y de este `compose.yaml`. El workflow
**BPMN staging** recibe dos SHA completos, construye las imágenes en GitHub,
las publica en GHCR y despliega la pareja exacta por digest en el runner Linux
`bmpn-staging`. Los paquetes pueden permanecer privados porque los crea el
repositorio que los descarga. El runner accede por HTTPS saliente.

El stack usa `~/bmpn-staging`, el proyecto Compose `bmpn-staging`, su propio
volumen de PostgreSQL y el puerto local `18080`. El primer despliegue genera
el `.env` con permisos privados. `images.env` guarda los digests desplegados.

Desde el computador con acceso SSH a la VM:

```powershell
ssh -N -L 18080:127.0.0.1:18080 estudiante@10.43.99.131
```

Abre `http://127.0.0.1:18080` mientras el túnel esté activo. Si el runner se
inició como servicio de usuario y Ubuntu no tiene habilitada la persistencia
de esa sesión, mantén una sesión de ese usuario abierta al desplegar.

## Producción

Prepara otra VM, registra un runner exclusivo con la etiqueta
`bmpn-production` en el repositorio privado y configura el proxy HTTPS que
publica el frontend. En `~/bmpn-production/.env`, genera credenciales propias
con permisos `600`. Configura `BMPN_REFRESH_COOKIE_SECURE=true`,
`DATOS_INICIALES=false` y el origen HTTPS real en `CORS_ORIGENES`.

Cuando el servidor esté preparado, configura el entorno `production` y
su aprobación de revisores si el plan de GitHub la permite. Activa la
variable del repositorio `PRODUCTION_DEPLOY_ENABLED=true` y ejecuta
**BPMN production** desde `main` con el par de digests de una ejecución
exitosa de staging.

El workflow crea un respaldo de PostgreSQL y verifica que se pueda listar
antes de actualizar frontend y backend. Flyway se ejecuta al arrancar el
backend. Si hubo una migración incompatible, prueba la restauración del
respaldo junto con los digests anteriores. La réplica física se configura
por separado.
