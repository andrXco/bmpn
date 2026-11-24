\set ON_ERROR_STOP on

BEGIN;

INSERT INTO empresa (nit, nombre, correo_contacto)
VALUES ('900000001-1', 'Empresa de prueba', 'contacto@prueba.local')
RETURNING id AS empresa_id \gset

INSERT INTO usuario (
    empresa_id, email, nombre, apellido, password_hash, rol_acceso
) VALUES (
    :empresa_id, 'admin@prueba.local', 'Admin', 'Prueba',
    '$2a$10$hashDePruebaNoUsableParaAutenticacion', 'ADMINISTRADOR'
)
RETURNING id AS usuario_id \gset

INSERT INTO rol_proceso (empresa_id, nombre, descripcion)
VALUES (:empresa_id, 'Analista', 'Rol usado por la prueba de base de datos')
RETURNING id AS rol_id \gset

INSERT INTO proceso (empresa_id, nombre, descripcion, categoria)
VALUES (:empresa_id, 'Proceso de prueba', 'Valida el esquema relacional', 'PRUEBA')
RETURNING id AS proceso_id \gset

INSERT INTO historial_proceso (proceso_id, usuario_id, accion, detalle)
VALUES (:proceso_id, :usuario_id, 'CREAR', 'Creacion dentro de la prueba de humo');

INSERT INTO pool (
    proceso_id, empresa_participante_id, nombre, tipo_participante, orden
) VALUES (
    :proceso_id, :empresa_id, 'Empresa de prueba', 'EMPRESA', 0
)
RETURNING id AS pool_id \gset

INSERT INTO lane (pool_id, rol_proceso_id, orden)
VALUES (:pool_id, :rol_id, 0)
RETURNING id AS lane_id \gset

INSERT INTO pool_rol_disponible (pool_id, rol_proceso_id)
VALUES (:pool_id, :rol_id);

INSERT INTO permiso_pool (
    pool_id, rol_acceso, puede_crear, puede_editar, puede_eliminar
) VALUES
    (:pool_id, 'ADMINISTRADOR', TRUE, TRUE, TRUE),
    (:pool_id, 'SOLO_LECTURA', FALSE, FALSE, FALSE);

INSERT INTO elemento_proceso (
    proceso_id, pool_id, lane_id, nombre, posicion_x, posicion_y
) VALUES (
    :proceso_id, :pool_id, :lane_id, 'Revisar solicitud', 120.00, 80.00
)
RETURNING id AS actividad_elemento_id \gset

INSERT INTO actividad (elemento_id, tipo_actividad)
VALUES (:actividad_elemento_id, 'USUARIO');

INSERT INTO elemento_proceso (
    proceso_id, pool_id, nombre, posicion_x, posicion_y
) VALUES (
    :proceso_id, :pool_id, 'Fin', 360.00, 80.00
)
RETURNING id AS evento_elemento_id \gset

INSERT INTO evento (elemento_id, tipo_evento, disparador)
VALUES (:evento_elemento_id, 'FIN', 'NINGUNO');

INSERT INTO arco (proceso_id, origen_id, destino_id, etiqueta)
VALUES (:proceso_id, :actividad_elemento_id, :evento_elemento_id, 'Finaliza');

DO $$
DECLARE
    cantidad INTEGER;
BEGIN
    SELECT count(*)
      INTO cantidad
      FROM elemento_proceso e
      JOIN proceso p ON p.id = e.proceso_id
      JOIN empresa em ON em.id = p.empresa_id
     WHERE em.nit = '900000001-1'
       AND p.nombre = 'Proceso de prueba';

    IF cantidad <> 2 THEN
        RAISE EXCEPTION 'La prueba esperaba 2 elementos y encontro %', cantidad;
    END IF;
END;
$$;

-- La base debe rechazar permisos de escritura para SOLO_LECTURA.
DO $$
BEGIN
    BEGIN
        UPDATE permiso_pool
           SET puede_editar = TRUE
         WHERE pool_id = (
             SELECT po.id
               FROM pool po
               JOIN proceso p ON p.id = po.proceso_id
              WHERE p.nombre = 'Proceso de prueba'
                AND po.orden = 0
         )
           AND rol_acceso = 'SOLO_LECTURA';
        RAISE EXCEPTION 'Se aceptaron permisos invalidos para SOLO_LECTURA';
    EXCEPTION
        WHEN check_violation THEN NULL;
    END;
END;
$$;

-- La base debe rechazar arcos que crucen pools.
INSERT INTO pool (
    proceso_id, nombre, tipo_participante, caja_negra, orden
) VALUES (
    :proceso_id, 'Participante externo', 'EXTERNO', FALSE, 1
)
RETURNING id AS pool_externo_id \gset

INSERT INTO elemento_proceso (
    proceso_id, pool_id, nombre, posicion_x, posicion_y
) VALUES (
    :proceso_id, :pool_externo_id, 'Inicio externo', 20.00, 20.00
)
RETURNING id AS evento_externo_id \gset

INSERT INTO evento (elemento_id, tipo_evento, disparador)
VALUES (:evento_externo_id, 'INICIO', 'NINGUNO');

DO $$
DECLARE
    proceso_prueba_id BIGINT;
    origen_prueba_id BIGINT;
    destino_externo_id BIGINT;
BEGIN
    SELECT p.id INTO proceso_prueba_id
      FROM proceso p
     WHERE p.nombre = 'Proceso de prueba';
    SELECT e.id INTO origen_prueba_id
      FROM elemento_proceso e
     WHERE e.proceso_id = proceso_prueba_id
       AND e.nombre = 'Revisar solicitud';
    SELECT e.id INTO destino_externo_id
      FROM elemento_proceso e
     WHERE e.proceso_id = proceso_prueba_id
       AND e.nombre = 'Inicio externo';

    BEGIN
        INSERT INTO arco (proceso_id, origen_id, destino_id)
        VALUES (proceso_prueba_id, origen_prueba_id, destino_externo_id);
        RAISE EXCEPTION 'Se acepto un arco que cruza pools';
    EXCEPTION
        WHEN raise_exception THEN
            IF SQLERRM = 'Se acepto un arco que cruza pools' THEN
                RAISE;
            END IF;
    END;
END;
$$;

ROLLBACK;

\echo 'OK: esquema BPMN, relaciones y rollback verificados.'
