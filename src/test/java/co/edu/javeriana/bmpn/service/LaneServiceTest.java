package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.lane.CrearLaneRequest;
import co.edu.javeriana.bmpn.dto.lane.EditarLaneRequest;
import co.edu.javeriana.bmpn.dto.lane.LaneResponse;
import co.edu.javeriana.bmpn.entity.Actividad;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Lane;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.RolProceso;
import co.edu.javeriana.bmpn.entity.TipoActividad;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.LaneRepository;

@ExtendWith(MockitoExtension.class)
class LaneServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long POOL_ID = 10L;
    private static final Long LANE_ID = 20L;
    private static final Long ROL_ID = 7L;
    private static final Long USUARIO_ID = 5L;

    @Mock
    private LaneRepository laneRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private PoolService poolService;

    @Mock
    private RolProcesoService rolProcesoService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    private LaneService laneService;

    private Empresa empresa;
    private Proceso proceso;
    private Pool pool;
    private RolProceso rol;

    @BeforeEach
    void prepararDatos() {
        laneService = new LaneService(laneRepository, usuarioService, procesoService, poolService,
                rolProcesoService, historialProcesoService, new ModelMapperConfig().modelMapper());

        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        proceso = new Proceso(empresa, "Vacaciones", "Solicitud", "RRHH");
        pool = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        proceso.agregarPool(pool);
        rol = new RolProceso(empresa, "Empleado", null);
        // En las pruebas no hay base de datos, asi que el id se asigna a mano
        ReflectionTestUtils.setField(rol, "id", ROL_ID);
    }

    private void usuarioConRol(RolAcceso rolAcceso) {
        Usuario usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", rolAcceso);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
    }

    private void procesoYPoolEncontrados() {
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(poolService.buscarActivo(POOL_ID, PROCESO_ID)).thenReturn(pool);
    }

    @Test
    void crearLane() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoYPoolEncontrados();
        when(rolProcesoService.buscarActivo(eq(ROL_ID), any())).thenReturn(rol);

        LaneResponse respuesta = laneService.crear(PROCESO_ID, POOL_ID, USUARIO_ID, new CrearLaneRequest(ROL_ID));

        assertThat(respuesta.isActivo()).isTrue();
        verify(laneRepository).save(any(Lane.class));
    }

    @Test
    void crearLaneConRolRepetido() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoYPoolEncontrados();
        when(rolProcesoService.buscarActivo(eq(ROL_ID), any())).thenReturn(rol);
        when(laneRepository.existsByPoolIdAndRolProcesoId(POOL_ID, ROL_ID)).thenReturn(true);

        assertThatThrownBy(() -> laneService.crear(PROCESO_ID, POOL_ID, USUARIO_ID, new CrearLaneRequest(ROL_ID)))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(laneRepository, never()).save(any());
    }

    @Test
    void crearLaneEnSistemaExterno() {
        usuarioConRol(RolAcceso.EDITOR);
        Pool externo = new Pool(null, "Servicio de correo", TipoParticipante.SISTEMA_EXTERNO, 1);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(poolService.buscarActivo(POOL_ID, PROCESO_ID)).thenReturn(externo);

        assertThatThrownBy(() -> laneService.crear(PROCESO_ID, POOL_ID, USUARIO_ID, new CrearLaneRequest(ROL_ID)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void eliminarLaneVacia() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);
        procesoYPoolEncontrados();
        Lane lane = new Lane(pool, rol, 0);
        when(laneRepository.findByIdAndPoolIdAndActivoTrue(LANE_ID, POOL_ID)).thenReturn(Optional.of(lane));

        laneService.eliminar(PROCESO_ID, POOL_ID, LANE_ID, USUARIO_ID);

        assertThat(lane.isActivo()).isFalse();
    }

    @Test
    void eliminarLaneConActividades() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);
        procesoYPoolEncontrados();
        Lane lane = new Lane(pool, rol, 0);
        Actividad actividad = new Actividad(proceso, pool, "Radicar", TipoActividad.USUARIO, BigDecimal.ONE, BigDecimal.ONE);
        actividad.asignarLane(lane);
        lane.getElementos().add(actividad);
        when(laneRepository.findByIdAndPoolIdAndActivoTrue(LANE_ID, POOL_ID)).thenReturn(Optional.of(lane));

        assertThatThrownBy(() -> laneService.eliminar(PROCESO_ID, POOL_ID, LANE_ID, USUARIO_ID))
                .isInstanceOf(SolicitudInvalidaException.class);
        assertThat(lane.isActivo()).isTrue();
    }

    @Test
    void eliminarSinPermiso() {
        usuarioConRol(RolAcceso.EDITOR);

        assertThatThrownBy(() -> laneService.eliminar(PROCESO_ID, POOL_ID, LANE_ID, USUARIO_ID))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void editarLaneCambiaElRol() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoYPoolEncontrados();
        Lane lane = new Lane(pool, rol, 0);
        RolProceso jefe = new RolProceso(empresa, "Jefe inmediato", null);
        when(laneRepository.findByIdAndPoolIdAndActivoTrue(LANE_ID, POOL_ID)).thenReturn(Optional.of(lane));
        when(rolProcesoService.buscarActivo(eq(8L), any())).thenReturn(jefe);

        laneService.editar(PROCESO_ID, POOL_ID, LANE_ID, USUARIO_ID, new EditarLaneRequest(8L));

        assertThat(lane.getRolProceso()).isEqualTo(jefe);
    }

    @Test
    void listarLanes() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        procesoYPoolEncontrados();
        when(laneRepository.findAllByPoolIdAndActivoTrueOrderByOrdenAsc(POOL_ID)).thenReturn(List.of(new Lane(pool, rol, 0)));

        assertThat(laneService.listar(PROCESO_ID, POOL_ID, USUARIO_ID)).hasSize(1);
    }
}
