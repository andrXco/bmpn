# BPMN

[![CI](https://github.com/andrXco/bmpn/actions/workflows/ci.yml/badge.svg)](https://github.com/andrXco/bmpn/actions/workflows/ci.yml)

Base de un proyecto académico configurado como API REST con Spring Boot y PostgreSQL.

## Requisitos

- Java 17 o superior (el proyecto compila para Java 17).
- Docker Desktop con Docker Compose.
- Git.

No es necesario instalar Maven: el repositorio incluye Maven Wrapper.

## Inicio rápido

1. Clona el repositorio y entra en la carpeta del proyecto.
2. Inicia PostgreSQL y espera a que este saludable:

   ```powershell
   docker compose up -d --wait
   ```

3. Verifica que el contenedor esté saludable:

   ```powershell
   docker compose ps
   ```

4. Inicia una vez la aplicacion para que Flyway cree el esquema versionado:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   Cuando termine el arranque, detenla con `Ctrl+C`.

5. Ejecuta la prueba de humo de PostgreSQL. Inserta un proceso BPMN minimo en
   una transaccion y hace `ROLLBACK`, asi que no deja datos:

   ```powershell
   .\scripts\database\database-smoke-test.ps1
   ```

6. Ejecuta las pruebas de Java:

   ```powershell
   .\mvnw.cmd clean test
   ```

7. Inicia la aplicacion para trabajar normalmente:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

En macOS o Linux reemplaza `.\mvnw.cmd` por `./mvnw`.

## Documentación interactiva con Swagger UI

Con la aplicación iniciada, abre [Swagger UI](http://localhost:8080/swagger-ui.html).
La página se genera a partir de los controladores, DTO y validaciones del
backend; no reemplaza los flujos de pruebas que se mantienen en Postman.

También puedes consultar la especificación OpenAPI directamente:

- JSON: `http://localhost:8080/v3/api-docs`
- YAML: `http://localhost:8080/v3/api-docs.yaml`

Desde Swagger UI puedes seleccionar un endpoint, usar **Try it out**, completar
los parámetros y el cuerpo JSON, y ejecutar una petición contra tu backend
local. La ruta de Swagger no guarda datos por sí misma: los cambios solo ocurren
si ejecutas un endpoint que crea, edita o desactiva información.

La autenticación actual es transitoria para la fase de desarrollo. En los
endpoints que la requieren, usa el `usuarioId` que devuelve `POST /api/sesiones`.
Cuando el proyecto incorpore Spring Security, esta parte se reemplazará por el
mecanismo de seguridad definitivo.

## Configuración local

El proyecto incluye valores únicamente para desarrollo local. Si necesitas cambiar el contenedor, copia `.env.example` como `.env` y ajusta los valores de PostgreSQL. Docker Compose lee ese archivo automáticamente.

Spring Boot utiliza los mismos valores locales de forma predeterminada. Para conectarlo a una configuración diferente, define estas variables en el sistema antes de iniciar la aplicación:

| Variable | Valor local predeterminado |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/bmpn` |
| `DB_USERNAME` | `bmpn` |
| `DB_PASSWORD` | `bmpn_local` |

El archivo `.env` está excluido de Git. Nunca guardes credenciales de producción en el repositorio.

## Comandos útiles

```powershell
# Detener PostgreSQL conservando los datos
docker compose down

# Ver registros de PostgreSQL
docker compose logs -f postgres

# Borrar también el volumen y todos los datos locales
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
