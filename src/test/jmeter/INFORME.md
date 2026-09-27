# Pruebas de carga con JMeter

Pregunta que se responde: **¿cuántas peticiones por segundo soporta la API en una infraestructura determinada?**

## Infraestructura

Todo se ejecutó en el mismo equipo (aplicación, base de datos y JMeter):

| Elemento | Detalle |
| --- | --- |
| Equipo | Portátil, AMD Ryzen 7 5800H (8 núcleos, 16 hilos), 15,4 GB de RAM |
| Sistema operativo | Windows 11 Pro |
| Aplicación | Spring Boot (jar), Java 21, puerto 8080, configuración por defecto |
| Base de datos | PostgreSQL 17 en Docker (el `compose.yaml` del proyecto) |
| Herramienta | Apache JMeter 5.6.3 en modo consola |

Como JMeter compite por la CPU con la aplicación y la base de datos, los resultados son un límite inferior: en un servidor dedicado la API debería soportar más.

## Plan de prueba

Archivo: `plan-carga-bmpn.jmx`.

1. **Preparación de datos** (una sola vez): registra una empresa y arma un proceso completo: rol de proceso, lane, actividad, gateway, arco y evento de envío.
2. **Usuarios concurrentes**: cada usuario repite, mientras dure la prueba, estas nueve peticiones:
   - `GET /api/procesos` y `GET /api/procesos/{id}`
   - `GET` de actividades, arcos, gateways, eventos y lanes del proceso
   - `GET /api/roles-proceso`
   - `POST /api/procesos/{id}/actividades` (crear una actividad en la lane)

La cantidad de usuarios, la rampa y la duración son parámetros (`-Jusuarios`, `-Jrampa`, `-Jduracion`).

## Cómo ejecutarlo

1. Levantar la base de datos y la aplicación:

   ```powershell
   docker compose up -d --wait
   .\mvnw.cmd spring-boot:run
   ```

2. Ejecutar un escenario desde la carpeta `bin` de JMeter (ejemplo con el escenario normal):

   ```powershell
   .\jmeter.bat -n -t <ruta>\src\test\jmeter\plan-carga-bmpn.jmx -l normal.csv -Jusuarios=50 -Jrampa=10 -Jduracion=60 -e -o reporte-normal
   ```

   `-e -o` genera un reporte HTML con gráficas en la carpeta `reporte-normal`.

Para ver o editar el plan en la interfaz gráfica: `jmeter.bat` y abrir el archivo `.jmx`.

## Escenarios y resultados

**Carga** (uso esperado del sistema):

| Escenario | Usuarios | Rampa | Duración | Peticiones | Peticiones/s | Promedio | p95 | Errores |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Carga normal | 50 | 10 s | 60 s | 23.501 | **384,1** | 117 ms | 226 ms | 0 % |
| Carga sostenida | 100 | 20 s | 180 s | 70.739 | **389,7** | 241 ms | 529 ms | 0 % |
| Pico | 300 | 5 s | 60 s | 37.904 | **615,1** | 458 ms | 885 ms | 0 % |

**Estrés** (se suben los usuarios hasta encontrar el tope):

| Escenario | Usuarios | Rampa | Duración | Peticiones | Peticiones/s | Promedio | p95 | Errores | CPU del equipo |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Estrés 1 | 500 | 10 s | 60 s | 34.255 | **547,9** | 812 ms | 1.261 ms | 0 % | 96 % |
| Estrés 2 | 800 | 10 s | 60 s | 39.727 | **626,2** | 1.135 ms | 2.154 ms | 0 % | 90 % |
| Estrés 3 | 1.200 | 10 s | 60 s | 37.830 | **593,7** | 1.806 ms | 3.446 ms | 0 % | 97 % |

*p95: el 95 % de las peticiones respondió en ese tiempo o menos.*

## Análisis

- **Ninguna petición falló** en ningún escenario, ni siquiera con 1.200 usuarios: la API no pierde peticiones, solo las atiende más lento.
- **El tope está alrededor de 600 peticiones por segundo.** Desde unos 300 usuarios, agregar más usuarios ya no aumenta las peticiones por segundo (615, 548, 626 y 594); lo único que crece es el tiempo de respuesta, porque las peticiones hacen fila.
- **El límite lo pone la CPU del equipo**, que estuvo entre 90 % y 97 % en las pruebas de estrés. Como JMeter, la aplicación y la base de datos compartían el mismo portátil, parte de esa CPU la consumía el propio JMeter.
- **Con 50 usuarios la API responde rápido**: 117 ms en promedio y el 95 % por debajo de 230 ms.
- **La prueba sostenida da menos peticiones por segundo que el pico** porque dura tres minutos y el listado de actividades se va haciendo lento: la prueba crea miles de actividades en el mismo proceso y ese listado no tiene paginación (pasó de unos 180 ms a unos 490 ms). Los demás endpoints se mantienen estables.

Posibles mejoras:

- Paginar el listado de actividades, como ya se hace con los procesos y los roles.
- Repetir la prueba con la aplicación y la base de datos en una máquina distinta a la de JMeter, para medir solo la capacidad del servidor.

## Conclusión

En un portátil con Ryzen 7 5800H y 16 GB de RAM, con la aplicación, la base de datos y JMeter en el mismo equipo:

- **La API sostiene unas 380 peticiones por segundo con el 95 % de las respuestas por debajo de medio segundo** (50 a 100 usuarios).
- **El tope es de unas 600 peticiones por segundo**, alcanzado desde unos 300 usuarios concurrentes y limitado por la CPU del equipo.
- **Por encima de ese punto no hay errores**, pero el tiempo de respuesta crece: con 1.200 usuarios el 95 % responde en menos de 3,5 s.

## Error encontrado durante las pruebas

Una primera ejecución mostró que `GET /api/procesos` **sin filtro de nombre** respondía 500: la consulta usaba `:nombre IS NULL OR LOWER(p.nombre) LIKE ...` y PostgreSQL no puede aplicar `LOWER` a un parámetro nulo. Se corrigió enviando un texto vacío cuando no hay filtro.
