package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.actividad.ActividadResponse;
import co.edu.javeriana.bmpn.dto.actividad.CrearActividadRequest;
import co.edu.javeriana.bmpn.dto.actividad.EditarActividadRequest;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
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
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.ActividadRepository;

@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long ACTIVIDAD_ID = 10L;
    private static final Long USUARIO_ID = 5L;
    private static final Long LANE_ID = 20L;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    @Mock
    private ArcoService arcoService;

    @Mock
    private LaneService laneService;

    private ActividadService actividadService;

    private Empresa empresa;
    private Proceso proceso;
    private Pool poolEmpresa;
    private Lane laneEmpleado;
    private Lane laneJefe;

    @BeforeEach
    void prepararDatos() {
        actividadService = new ActividadService(actividadRepository, procesoService,
                usuarioService, historialProcesoService, arcoService, laneService,
                new ModelMapperConfig().modelMapper());

        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        proceso = new Proceso(empresa, "Solicitud de vacaciones", "Proceso de ejemplo", "RRHH");
        poolEmpresa = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        proceso.agregarPool(poolEmpresa);
        laneEmpleado = new Lane(poolEmpresa, new RolProceso(empresa, "Empleado", null), 0);
        laneJefe = new Lane(poolEmpresa, new RolProceso(empresa, "Jefe inmediato", null), 1);
    }

    private Usuario usuarioConRol(RolAcceso rol) {
        Usuario usuario = new Usuario(empresa, "usuario@demo.co", "Ana", "Paz", "hash", rol);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
        return usuario;
    }

    private CrearActividadRequest solicitudCrear(String nombre, Long poolId) {
        return new CrearActividadRequest(nombre, TipoActividad.USUARIO,
                new BigDecimal("100.00"), new BigDecimal("50.00"), LANE_ID, poolId);
    }

    private Actividad actividadExistente() {
        Actividad actividad = new Actividad(proceso, poolEmpresa, "Radicar solicitud", TipoActividad.USUARIO,
                new BigDecimal("100.00"), new BigDecimal("50.00"));
        actividad.asignarLane(laneEmpleado);
        return actividad;
    }

    @Test
    void crearActividadValida() {
        Usuario editor = usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(laneService.buscarActiva(eq(LANE_ID), any())).thenReturn(laneEmpleado);

        ActividadResponse respuesta = actividadService.crear(PROCESO_ID, USUARIO_ID,
                solicitudCrear("  Radicar solicitud  ", null));

        assertThat(respuesta.getNombre()).isEqualTo("Radicar solicitud");
        assertThat(respuesta.getTipoActividad()).isEqualTo(TipoActividad.USUARIO);
        assertThat(respuesta.getPosicionX()).isEqualTo(new BigDecimal("100.00"));
        assertThat(respuesta.isActivo()).isTrue();
        ArgumentCaptor<Actividad> guardada = ArgumentCaptor.forClass(Actividad.class);
        verify(actividadRepository).save(guardada.capture());
        assertThat(guardada.getValue().getLane()).isEqualTo(laneEmpleado);
        verify(historialProcesoService).registrar(eq(proceso), eq(editor),
                eq(AccionHistorial.CREACION), anyString());
    }

    @Test
    void crearConLaneDeOtroPool() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(laneService.buscarActiva(eq(LANE_ID), any()))
                .thenThrow(new RecursoNoEncontradoException("Lane no encontrada en el pool"));

        CrearActividadRequest solicitud = solicitudCrear("Radicar solicitud", null);
        assertThatThrownBy(() -> actividadService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearSinPermiso() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);

        CrearActividadRequest solicitud = solicitudCrear("Radicar solicitud", null);
        assertThatThrownBy(() -> actividadService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearNombreRepetido() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(actividadRepository.existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(
                PROCESO_ID, "Radicar solicitud")).thenReturn(true);

        CrearActividadRequest solicitud = solicitudCrear("Radicar solicitud", null);
        assertThatThrownBy(() -> actividadService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearPoolInexistente() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);

        CrearActividadRequest solicitud = solicitudCrear("Radicar solicitud", 99L);
        assertThatThrownBy(() -> actividadService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void editarActividad() {
        Usuario editor = usuarioConRol(RolAcceso.EDITOR);
        Actividad actividad = actividadExistente();
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(actividadRepository.findByIdAndProcesoIdAndActivoTrue(ACTIVIDAD_ID, PROCESO_ID))
                .thenReturn(Optional.of(actividad));
        when(laneService.buscarActiva(eq(LANE_ID), any())).thenReturn(laneJefe);

        EditarActividadRequest cambios = new EditarActividadRequest("Revisar solicitud",
                TipoActividad.MANUAL, new BigDecimal("300.00"), new BigDecimal("80.00"), LANE_ID);
        ActividadResponse respuesta = actividadService.editar(PROCESO_ID, ACTIVIDAD_ID, USUARIO_ID, cambios);

        assertThat(respuesta.getNombre()).isEqualTo("Revisar solicitud");
        assertThat(respuesta.getTipoActividad()).isEqualTo(TipoActividad.MANUAL);
        assertThat(respuesta.getPosicionX()).isEqualTo(new BigDecimal("300.00"));
        assertThat(actividad.getLane()).isEqualTo(laneJefe);
        verify(historialProcesoService).registrar(eq(proceso), eq(editor),
                eq(AccionHistorial.ACTUALIZACION), anyString());
    }

    @Test
    void editarActividadInexistente() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(actividadRepository.findByIdAndProcesoIdAndActivoTrue(ACTIVIDAD_ID, PROCESO_ID))
                .thenReturn(Optional.empty());

        EditarActividadRequest cambios = new EditarActividadRequest("Revisar solicitud",
                TipoActividad.MANUAL, BigDecimal.ONE, BigDecimal.ONE, LANE_ID);

        assertThatThrownBy(() -> actividadService.editar(PROCESO_ID, ACTIVIDAD_ID, USUARIO_ID, cambios))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarActividad() {
        Usuario administrador = usuarioConRol(RolAcceso.ADMINISTRADOR);
        Actividad actividad = actividadExistente();
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(actividadRepository.findByIdAndProcesoIdAndActivoTrue(ACTIVIDAD_ID, PROCESO_ID))
                .thenReturn(Optional.of(actividad));

        actividadService.eliminar(PROCESO_ID, ACTIVIDAD_ID, USUARIO_ID);

        assertThat(actividad.isActivo()).isFalse();
        verify(actividadRepository, never()).delete(any());
        verify(arcoService).desactivarArcosDeElemento(ACTIVIDAD_ID);
        verify(historialProcesoService).registrar(eq(proceso), eq(administrador),
                eq(AccionHistorial.ELIMINACION), anyString());
    }

    @Test
    void eliminarSinPermiso() {
        usuarioConRol(RolAcceso.EDITOR);

        assertThatThrownBy(() -> actividadService.eliminar(PROCESO_ID, ACTIVIDAD_ID, USUARIO_ID))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void listarActividades() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(actividadRepository.listarActivasPorProceso(PROCESO_ID))
                .thenReturn(List.of(actividadExistente()));

        List<ActividadResponse> actividades = actividadService.listar(PROCESO_ID, USUARIO_ID);

        assertThat(actividades).hasSize(1);
        assertThat(actividades.get(0).getNombre()).isEqualTo("Radicar solicitud");
    }
}
