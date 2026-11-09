# BPMN

[![CI](https://github.com/andrXco/bmpn/actions/workflows/ci.yml/badge.svg)](https://github.com/andrXco/bmpn/actions/workflows/ci.yml)

Editor y visor multiempresa de procesos BPMN. API REST con Spring Boot y PostgreSQL.

## Requisitos

- Docker Desktop con Docker Compose.
- Java 17 y Git, solo si vas a desarrollar o correr las pruebas.

No es necesario instalar Maven: el repositorio incluye Maven Wrapper.

## Levantar el servicio con Docker

```powershell
docker compose up -d --build --wait
```

Levanta PostgreSQL y la API en `http://localhost:8080/api`. Flyway crea el esquema
al arrancar. El frontend tiene su propio Docker en el repositorio
[bmpn-react](https://github.com/andrXco/bmpn-react) y llama a esta API; CORS permite
el origen `http://localhost:4200` (se cambia con `CORS_ORIGENES`).

Con la base vacia se cargan datos de ejemplo: la empresa BitWeb Demo con el
proceso "Solicitud de vacaciones" y los usuarios `admin@bitweb.co` (id 1),
`editor@bitweb.co` y `lector@bitweb.co`, ademas de la empresa Cliente Demo. La
clave de todos es `BitWeb2026` y puede cambiarse con `DATOS_INICIALES_CLAVE`.
Para no cargarlos, usa `DATOS_INICIALES=false`.

La API requiere un access token Bearer. El `usuarioId` de las rutas existentes
debe coincidir con el sujeto del token; el backend rechaza identificadores que
no correspondan a la sesión autenticada.

Prueba rápida en PowerShell:

```powershell
$credenciales = @{ email = "admin@bitweb.co"; password = "BitWeb2026" } | ConvertTo-Json
$sesion = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/sesiones" `
  -ContentType "application/json" -Body $credenciales -SessionVariable cookies
$headers = @{ Authorization = "Bearer $($sesion.accessToken)" }
Invoke-RestMethod -Uri "http://localhost:8080/api/procesos?usuarioId=$($sesion.usuarioId)" `
  -Headers $headers
```

## Desarrollo local

1. Inicia solo PostgreSQL:

   ```powershell
   docker compose up -d --wait postgres
   ```

2. Ejecuta las pruebas:

   ```powershell
   .\mvnw.cmd clean test
   ```

3. Inicia la aplicacion:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

En macOS o Linux reemplaza `.\mvnw.cmd` por `./mvnw`.

## Documentación interactiva con Swagger UI

Con la aplicación iniciada, abre [Swagger UI](http://localhost:8080/swagger-ui/index.html).
La página se genera a partir de los controladores, DTO y validaciones del
backend; no reemplaza los flujos de pruebas que se mantienen en Postman.

También puedes consultar la especificación OpenAPI directamente:

- JSON: `http://localhost:8080/v3/api-docs`
- YAML: `http://localhost:8080/v3/api-docs.yaml`

Desde Swagger UI puedes seleccionar un endpoint, usar **Try it out**, completar
los parámetros y el cuerpo JSON, y ejecutar una petición contra tu backend
local. La ruta de Swagger no guarda datos por sí misma: los cambios solo ocurren
si ejecutas un endpoint que crea, edita o desactiva información.

El login devuelve un access token JWT de 15 minutos y establece un refresh token
opaco en una cookie `HttpOnly`, `SameSite=Strict`. La renovación rota el refresh
token; su hash se conserva en PostgreSQL para poder revocarlo al cerrar sesión o
detectar reutilización. Angular conserva el access token solo en memoria y lo
renueva cuando recibe un `401`.

La prueba de humo de PostgreSQL (`.\scripts\database\database-smoke-test.ps1`)
inserta un proceso minimo en una transaccion y hace `ROLLBACK`, asi que no deja datos.

## Configuración local

Los valores del repositorio son solo para desarrollo local. Docker Compose lee
un archivo `.env` si existe (excluido de Git). Variables disponibles:

| Variable | Valor local predeterminado |
| --- | --- |
| `POSTGRES_PORT` | `5432` |
| `BMPN_JWT_SECRET` | Requerida; clave aleatoria de al menos 32 bytes, fuera de Git |
| `BMPN_JWT_ACCESS_EXPIRATION` | `PT15M` |
| `BMPN_REFRESH_TOKEN_EXPIRATION` | `P5D` |
| `BMPN_REFRESH_COOKIE_SECURE` | `false` en HTTP local; usar `true` detrás de HTTPS |
| `CORS_ORIGENES` | `http://localhost:4200` |
| `APP_PORT` | `8080` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/bmpn` |
| `DB_USERNAME` | `bmpn` |
| `DB_PASSWORD` | `bmpn_local` |
| `DATOS_INICIALES` | `true` |

Genera `BMPN_JWT_SECRET` fuera del repositorio y guárdala en `.env` (ignorado
por Git) o en el entorno antes de iniciar Docker Compose. En PowerShell puedes
crear una clave aleatoria para la sesión actual con:

```powershell
$env:BMPN_JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

En despliegue con HTTPS, configura `BMPN_REFRESH_COOKIE_SECURE=true`.

Nunca guardes credenciales de produccion en el repositorio.

## Comandos útiles

```powershell
# Detener los contenedores conservando los datos
docker compose down

# Ver registros de la API
docker compose logs -f app

# Borrar tambien el volumen y todos los datos locales
docker compose down -v
```

El último comando elimina información local y debe utilizarse con cuidado.

## Estructura principal

```text
src/main/java/co/edu/javeriana/bmpn/   Código Java
src/main/resources/application.properties
docs/                                  Documentación y diagramas
```

La capa de presentación del backend expone respuestas JSON bajo `/api`. La
interfaz de usuario se desarrolla como un frontend independiente que consume
estos endpoints.

### Endpoints disponibles

La API expone operaciones para empresas, autenticación, usuarios, procesos,
actividades, gateways y arcos. La lista actualizada de rutas, cuerpos JSON,
respuestas y códigos HTTP se consulta en Swagger UI.

Para probar el flujo completo con Postman, importa los archivos de
[`postman/`](postman/README.md). La colección está organizada por módulos,
encadena los identificadores creados y cubre todos los endpoints actualmente
implementados.

La API no conserva una sesión HTTP. En esta etapa, el cliente envía el
identificador del usuario solicitante y la capa de servicios obtiene desde la
base de datos su empresa y rol. Es un contrato transitorio para probar la API
antes de integrar Spring Security; no reemplaza un mecanismo de autenticación
de producción. Los errores se entregan como respuestas JSON compatibles con
`ProblemDetail`.

## Esquema de base de datos

La migracion inicial esta en
`src/main/resources/db/migration/V1__crear_esquema_bpmn.sql`. Implementa el
modelo E/R documentado: empresas, usuarios, procesos, historial, colaboracion,
pools, lanes, elementos BPMN, arcos y mensajes. Incluye claves foraneas,
unicidad y borrado en cascada para dependencias fisicas.

La migracion `V2__quitar_triggers_de_validacion.sql` elimina los triggers de
validacion de la V1: las reglas de negocio (por ejemplo, que un arco no cruce
pools o que una actividad pertenezca a una lane) se validan en la capa de
servicios.

Las eliminaciones funcionales continuan siendo logicas mediante `activo`. Los
`ON DELETE` se reservan para operaciones fisicas de mantenimiento y pruebas.

## Convenciones del equipo

- El paquete base es `co.edu.javeriana.bmpn`.
- Las credenciales personales no se suben a Git.
- Antes de entregar cambios, ejecuta `.\mvnw.cmd clean test`.
- Los cambios futuros de estructura de base de datos se versionan con Flyway.

## Integración continua

GitHub Actions ejecuta automáticamente `clean verify` en cada `push` y `pull
request`. El workflow utiliza Java 17 y crea una instancia temporal de
PostgreSQL 17, por lo que las migraciones de Flyway y las pruebas de integración
no dependen de una base de datos externa. Los reportes de Surefire quedan
disponibles como artefacto durante siete días, incluso cuando una prueba falla.
