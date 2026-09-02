# BPMN

Base de un proyecto académico configurado con Spring Boot, Thymeleaf y PostgreSQL.

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

## Esquema de base de datos

La migracion inicial esta en
`src/main/resources/db/migration/V1__crear_esquema_bpmn.sql`. Implementa el
modelo E/R documentado: empresas, usuarios, procesos, historial, colaboracion,
pools, lanes, elementos BPMN, arcos y mensajes. Incluye claves foraneas,
unicidad, borrado en cascada para dependencias fisicas y validaciones entre
tablas.

Las eliminaciones funcionales continuan siendo logicas mediante `activo`. Los
`ON DELETE` se reservan para operaciones fisicas de mantenimiento y pruebas.

## Convenciones del equipo

- El paquete base es `co.edu.javeriana.bmpn`.
- Las credenciales personales no se suben a Git.
- Antes de entregar cambios, ejecuta `.\mvnw.cmd clean test`.
- Los cambios futuros de estructura de base de datos se versionan con Flyway.
