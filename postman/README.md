# Pruebas de la API con Postman

## Archivos que se comparten

- `BMPN.postman_collection.json`: colección con todos los endpoints actualmente implementados.
- `BPMN.local.postman_environment.json`: environment local con `baseUrl` igual a `http://localhost:8080`.

No incluye contraseñas reales, identificadores persistentes ni credenciales de
producción. Los identificadores de usuario, proceso, actividad, gateway y arco
se crean y guardan durante la ejecución de la colección.

## Ejecución

1. Inicia PostgreSQL con `docker compose up -d --wait`.
2. Inicia el backend con `./mvnw spring-boot:run` o `./mvnw.cmd spring-boot:run`.
3. En Postman importa ambos archivos de esta carpeta.
4. Selecciona el environment **BPMN local**.
5. Ejecuta los folders en el orden numerado, o usa Collection Runner desde el
   primer request hasta el último.

La solicitud inicial usa valores únicos para NIT y correos, por lo que el flujo
puede repetirse sin recibir un conflicto por datos de una ejecución anterior.
Cada ejecución conserva la empresa de prueba, porque la API aún no expone una
operación para eliminar empresas; úsala contra la base local de desarrollo.

## Alcance de las pruebas

La colección verifica los códigos HTTP esperados y encadena los IDs necesarios
para el flujo feliz: empresa, sesión, proceso, actividad, gateway, arco y
limpieza del proceso. Los casos de validación y error se prueban manualmente
desde Swagger UI o duplicando una solicitud y cambiando intencionalmente un
campo requerido.
