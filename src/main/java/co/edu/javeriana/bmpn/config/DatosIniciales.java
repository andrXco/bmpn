package co.edu.javeriana.bmpn.config;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.javeriana.bmpn.dto.actividad.CrearActividadRequest;
import co.edu.javeriana.bmpn.dto.arco.CrearArcoRequest;
import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.dto.evento.CrearEventoRequest;
import co.edu.javeriana.bmpn.dto.gateway.CrearGatewayRequest;
import co.edu.javeriana.bmpn.dto.lane.CrearLaneRequest;
import co.edu.javeriana.bmpn.dto.pool.CrearPoolRequest;
import co.edu.javeriana.bmpn.dto.proceso.CrearProcesoRequest;
import co.edu.javeriana.bmpn.dto.procesocompartido.CompartirProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.CrearRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.usuario.RegistrarUsuarioRequest;
import co.edu.javeriana.bmpn.entity.DisparadorEvento;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoActividad;
import co.edu.javeriana.bmpn.entity.TipoEvento;
import co.edu.javeriana.bmpn.entity.TipoGateway;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.service.ActividadService;
import co.edu.javeriana.bmpn.service.ArcoService;
import co.edu.javeriana.bmpn.service.EmpresaService;
import co.edu.javeriana.bmpn.service.EventoService;
import co.edu.javeriana.bmpn.service.GatewayService;
import co.edu.javeriana.bmpn.service.LaneService;
import co.edu.javeriana.bmpn.service.PoolService;
import co.edu.javeriana.bmpn.service.ProcesoCompartidoService;
import co.edu.javeriana.bmpn.service.ProcesoService;
import co.edu.javeriana.bmpn.service.RolProcesoService;
import co.edu.javeriana.bmpn.service.UsuarioService;

// Carga una empresa de ejemplo con el proceso de solicitud de vacaciones.
// Usa los servicios para que los datos pasen por las mismas validaciones de la API.
@Configuration
@ConditionalOnProperty(name = "app.datos-iniciales", havingValue = "true")
public class DatosIniciales {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;
    private final RolProcesoService rolProcesoService;
    private final ProcesoService procesoService;
    private final PoolService poolService;
    private final LaneService laneService;
    private final EventoService eventoService;
    private final ActividadService actividadService;
    private final GatewayService gatewayService;
    private final ArcoService arcoService;
    private final ProcesoCompartidoService procesoCompartidoService;

    public DatosIniciales(EmpresaService empresaService, UsuarioService usuarioService,
                          RolProcesoService rolProcesoService, ProcesoService procesoService,
                          PoolService poolService, LaneService laneService, EventoService eventoService,
                          ActividadService actividadService, GatewayService gatewayService,
                          ArcoService arcoService, ProcesoCompartidoService procesoCompartidoService) {
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.rolProcesoService = rolProcesoService;
        this.procesoService = procesoService;
        this.poolService = poolService;
        this.laneService = laneService;
        this.eventoService = eventoService;
        this.actividadService = actividadService;
        this.gatewayService = gatewayService;
        this.arcoService = arcoService;
        this.procesoCompartidoService = procesoCompartidoService;
    }

    @Bean
    CommandLineRunner cargarDatosIniciales(@Value("${app.datos-iniciales.clave}") String clave) {
        return args -> {
            if (empresaService.hayEmpresasRegistradas()) {
                return;
            }
            Long adminId = empresaService.registrar(new RegistrarEmpresaRequest("900100200", "BitWeb Demo",
                    "contacto@bitweb.co", "Laura", "Gomez", "admin@bitweb.co", clave))
                    .getAdministradorInicial().getId();
            usuarioService.registrar(adminId, new RegistrarUsuarioRequest("editor@bitweb.co", "Carlos",
                    "Ruiz", clave, RolAcceso.EDITOR));
            usuarioService.registrar(adminId, new RegistrarUsuarioRequest("lector@bitweb.co", "Sofia",
                    "Mejia", clave, RolAcceso.SOLO_LECTURA));

            Long clienteId = empresaService.registrar(new RegistrarEmpresaRequest("800300400", "Cliente Demo",
                    "contacto@cliente.co", "Andres", "Torres", "admin@cliente.co", clave)).getId();

            cargarProcesoDeVacaciones(adminId, clienteId);
            log.info("Datos de ejemplo cargados (admin@bitweb.co, editor@bitweb.co, lector@bitweb.co)");
        };
    }

    private void cargarProcesoDeVacaciones(Long adminId, Long clienteId) {
        Long empleadoId = rolProcesoService.crear(adminId,
                new CrearRolProcesoRequest("Empleado", "Quien solicita las vacaciones")).getId();
        Long jefeId = rolProcesoService.crear(adminId,
                new CrearRolProcesoRequest("Jefe inmediato", "Quien aprueba o rechaza")).getId();

        var proceso = procesoService.crear(adminId, new CrearProcesoRequest("Solicitud de vacaciones",
                "Un empleado pide vacaciones y su jefe decide", "Talento humano"));
        Long procesoId = proceso.getId();
        Long poolId = proceso.getPoolPropietarioId();

        Long laneEmpleado = laneService.crear(procesoId, poolId, adminId, new CrearLaneRequest(empleadoId)).getId();
        Long laneJefe = laneService.crear(procesoId, poolId, adminId, new CrearLaneRequest(jefeId)).getId();

        Long inicio = evento(procesoId, adminId, "Necesita vacaciones", TipoEvento.INICIO, laneEmpleado, 50);
        Long diligenciar = actividad(procesoId, adminId, "Diligenciar solicitud", laneEmpleado, 150);
        Long revisar = actividad(procesoId, adminId, "Revisar solicitud", laneJefe, 300);
        Long decision = gatewayService.crear(procesoId, adminId, new CrearGatewayRequest("Aprobada?",
                TipoGateway.EXCLUSIVO, posicion(450), posicion(250), null)).getId();
        Long registrar = actividad(procesoId, adminId, "Registrar vacaciones", laneJefe, 600);
        Long aprobada = evento(procesoId, adminId, "Vacaciones aprobadas", TipoEvento.FIN, laneJefe, 750);
        Long rechazada = evento(procesoId, adminId, "Solicitud rechazada", TipoEvento.FIN, laneJefe, 600);

        arco(procesoId, adminId, inicio, diligenciar, null);
        arco(procesoId, adminId, diligenciar, revisar, null);
        arco(procesoId, adminId, revisar, decision, null);
        arco(procesoId, adminId, decision, registrar, "Si");
        arco(procesoId, adminId, decision, rechazada, "No");
        arco(procesoId, adminId, registrar, aprobada, null);

        // La otra empresa queda invitada y participa como cliente del proceso
        procesoCompartidoService.compartir(procesoId, adminId, new CompartirProcesoRequest(clienteId));
        CrearPoolRequest poolCliente = new CrearPoolRequest();
        poolCliente.setNombre("Cliente Demo");
        poolCliente.setTipoParticipante(TipoParticipante.CLIENTE);
        poolCliente.setEmpresaParticipanteId(clienteId);
        poolService.crear(procesoId, adminId, poolCliente);
    }

    private Long evento(Long procesoId, Long usuarioId, String nombre, TipoEvento tipo, Long laneId, int x) {
        CrearEventoRequest request = new CrearEventoRequest();
        request.setNombre(nombre);
        request.setTipoEvento(tipo);
        request.setDisparador(DisparadorEvento.NINGUNO);
        request.setLaneId(laneId);
        request.setPosicionX(posicion(x));
        request.setPosicionY(posicion(100));
        return eventoService.crear(procesoId, usuarioId, request).getId();
    }

    private Long actividad(Long procesoId, Long usuarioId, String nombre, Long laneId, int x) {
        return actividadService.crear(procesoId, usuarioId, new CrearActividadRequest(nombre,
                TipoActividad.USUARIO, posicion(x), posicion(100), laneId, null)).getId();
    }

    private void arco(Long procesoId, Long usuarioId, Long origenId, Long destinoId, String condicion) {
        arcoService.crear(procesoId, usuarioId, new CrearArcoRequest(origenId, destinoId, condicion, condicion));
    }

    private BigDecimal posicion(int valor) {
        return BigDecimal.valueOf(valor);
    }
}
