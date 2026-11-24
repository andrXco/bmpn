-- Las reglas de negocio se validan en la capa de servicios, no en la base de datos.
-- Se eliminan los triggers y la funcion de validacion creados en la V1.

DROP TRIGGER IF EXISTS tr_historial_empresa ON historial_proceso;
DROP TRIGGER IF EXISTS tr_compartido_empresas ON proceso_compartido;
DROP TRIGGER IF EXISTS tr_lane_empresa_rol ON lane;
DROP TRIGGER IF EXISTS tr_pool_rol_empresa ON pool_rol_disponible;
DROP TRIGGER IF EXISTS tr_actividad_lane_subtipo ON actividad;
DROP TRIGGER IF EXISTS tr_evento_subtipo ON evento;
DROP TRIGGER IF EXISTS tr_gateway_subtipo ON gateway;
DROP TRIGGER IF EXISTS tr_arco_mismo_pool ON arco;

DROP FUNCTION IF EXISTS validar_relaciones_bpmn();
