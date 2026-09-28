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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.arco.ArcoResponse;
import co.edu.javeriana.bmpn.dto.arco.CrearArcoRequest;
import co.edu.javeriana.bmpn.dto.arco.EditarArcoRequest;
import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Actividad;
import co.edu.javeriana.bmpn.entity.Arco;
import co.edu.javeriana.bmpn.entity.ElementoProceso;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Gateway;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoActividad;
import co.edu.javeriana.bmpn.entity.TipoGateway;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.ArcoRepository;

@ExtendWith(MockitoExtension.class)
class ArcoServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long USUARIO_ID = 5L;
    private static final Long ARCO_ID = 50L;

    @Mock
    private ArcoRepository arcoRepository;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ElementoProcesoService elementoProcesoService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    private ArcoService arcoService;

    private Empresa empresa;
    private Proceso proceso;
    private Actividad radicar;
    private Actividad revisar;
    private Gateway decision;
    private Actividad actividadCliente;

    @BeforeEach
    void prepararDatos() {
        arcoService = new ArcoService(arcoRepository, procesoService, usuarioService,
                elementoProcesoService, historialProcesoService, new ModelMapperConfig().modelMapper());

        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        proceso = new Proceso(empresa, "Solicitud de vacaciones", "Proceso de ejemplo", "RRHH");
        Pool poolEmpresa = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        Pool poolCliente = new Pool(null, "Cliente", TipoParticipante.CLIENTE, 1);
        asignarId(proceso, PROCESO_ID);
        asignarId(poolEmpresa, 10L);
        asignarId(poolCliente, 20L);

        radicar = new Actividad(proceso, poolEmpresa, "Radicar", TipoActividad.USUARIO, BigDecimal.ONE, BigDecimal.ONE);
        revisar = new Actividad(proceso, poolEmpresa, "Revisar", TipoActividad.USUARIO, BigDecimal.ONE, BigDecimal.ONE);
        decision = new Gateway(proceso, poolEmpresa, "Aprobada?", TipoGateway.EXCLUSIVO, BigDecimal.ONE, BigDecimal.ONE);
        actividadCliente = new Actividad(proceso, poolCliente, "Pagar", TipoActividad.USUARIO, BigDecimal.ONE, BigDecimal.ONE);
        asignarId(radicar, 1L);
        asignarId(revisar, 2L);
        asignarId(decision, 3L);
        asignarId(actividadCliente, 4L);
    }

    // En las pruebas no hay base de datos, asi que el id se asigna a mano
    private void asignarId(Object entidad, Long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
    }

    private Usuario usuarioConRol(RolAcceso rol) {
        Usuario usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", rol);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
        return usuario;
    }

    private void procesoEncontrado() {
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
    }

    private void elementosEncontrados(ElementoProceso origen, ElementoProceso destino) {
        when(elementoProcesoService.buscarActivoDelProceso(origen.getId(), PROCESO_ID)).thenReturn(origen);
        when(elementoProcesoService.buscarActivoDelProceso(destino.getId(), PROCESO_ID)).thenReturn(destino);
    }

    @Test
    void crearArco() {
        Usuario editor = usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();
        elementosEncontrados(radicar, revisar);

        ArcoResponse respuesta = arcoService.crear(PROCESO_ID, USUARIO_ID,
                new CrearArcoRequest(1L, 2L, " Enviar ", null));

        assertThat(respuesta.getOrigenId()).isEqualTo(1L);
        assertThat(respuesta.getDestinoId()).isEqualTo(2L);
        assertThat(respuesta.getEtiqueta()).isEqualTo("Enviar");
        verify(arcoRepository).save(any(Arco.class));
        verify(historialProcesoService).registrar(eq(proceso), eq(editor),
                eq(AccionHistorial.CREACION), anyString());
    }

    @Test
    void crearSinPermiso() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);

        CrearArcoRequest solicitud = new CrearArcoRequest(1L, 2L, null, null);
        assertThatThrownBy(() -> arcoService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(arcoRepository, never()).save(any());
    }

    @Test
    void crearArcoHaciaSiMismo() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();

        CrearArcoRequest solicitud = new CrearArcoRequest(1L, 1L, null, null);
        assertThatThrownBy(() -> arcoService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void crearArcoEntrePoolsDistintos() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();
        elementosEncontrados(radicar, actividadCliente);

        CrearArcoRequest solicitud = new CrearArcoRequest(1L, 4L, null, null);
        assertThatThrownBy(() -> arcoService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(arcoRepository, never()).save(any());
    }

    @Test
    void crearArcoRepetido() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();
        elementosEncontrados(radicar, revisar);
        when(arcoRepository.findByProcesoIdAndOrigenIdAndDestinoId(PROCESO_ID, 1L, 2L))
                .thenReturn(Optional.of(new Arco(proceso, radicar, revisar, null, null)));

        CrearArcoRequest solicitud = new CrearArcoRequest(1L, 2L, null, null);
        assertThatThrownBy(() -> arcoService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void crearReactivaArcoEliminado() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();
        elementosEncontrados(radicar, revisar);
        Arco eliminado = new Arco(proceso, radicar, revisar, null, null);
        eliminado.desactivar();
        when(arcoRepository.findByProcesoIdAndOrigenIdAndDestinoId(PROCESO_ID, 1L, 2L))
                .thenReturn(Optional.of(eliminado));

        ArcoResponse respuesta = arcoService.crear(PROCESO_ID, USUARIO_ID,
                new CrearArcoRequest(1L, 2L, null, null));

        assertThat(eliminado.isActivo()).isTrue();
        assertThat(respuesta.isActivo()).isTrue();
        verify(arcoRepository, never()).save(any());
    }

    @Test
    void crearConCondicionDesdeActividad() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();
        elementosEncontrados(radicar, revisar);

        CrearArcoRequest solicitud = new CrearArcoRequest(1L, 2L, null, "Si");
        assertThatThrownBy(() -> arcoService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void crearConCondicionDesdeGateway() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();
        elementosEncontrados(decision, revisar);

        ArcoResponse respuesta = arcoService.crear(PROCESO_ID, USUARIO_ID,
                new CrearArcoRequest(3L, 2L, null, "Si"));

        assertThat(respuesta.getCondicion()).isEqualTo("Si");
    }

    @Test
    void editarArco() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();
        Arco arco = new Arco(proceso, radicar, revisar, null, null);
        asignarId(arco, ARCO_ID);
        when(arcoRepository.findByIdAndProcesoIdAndActivoTrue(ARCO_ID, PROCESO_ID)).thenReturn(Optional.of(arco));
        elementosEncontrados(radicar, decision);

        ArcoResponse respuesta = arcoService.editar(PROCESO_ID, ARCO_ID, USUARIO_ID,
                new EditarArcoRequest(1L, 3L, "Decidir", null));

        assertThat(respuesta.getDestinoId()).isEqualTo(3L);
        assertThat(respuesta.getEtiqueta()).isEqualTo("Decidir");
    }

    @Test
    void editarArcoInexistente() {
        usuarioConRol(RolAcceso.EDITOR);
        procesoEncontrado();
        when(arcoRepository.findByIdAndProcesoIdAndActivoTrue(ARCO_ID, PROCESO_ID)).thenReturn(Optional.empty());

        EditarArcoRequest solicitud = new EditarArcoRequest(1L, 2L, null, null);
        assertThatThrownBy(() -> arcoService.editar(PROCESO_ID, ARCO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarArcoAvisaElementosDesconectados() {
        Usuario administrador = usuarioConRol(RolAcceso.ADMINISTRADOR);
        procesoEncontrado();
        Arco arco = new Arco(proceso, radicar, revisar, null, null);
        when(arcoRepository.findByIdAndProcesoIdAndActivoTrue(ARCO_ID, PROCESO_ID)).thenReturn(Optional.of(arco));

        AdvertenciasResponse respuesta = arcoService.eliminar(PROCESO_ID, ARCO_ID, USUARIO_ID);

        assertThat(arco.isActivo()).isFalse();
        assertThat(respuesta.getAdvertencias()).hasSize(2);
        verify(historialProcesoService).registrar(eq(proceso), eq(administrador),
                eq(AccionHistorial.ELIMINACION), anyString());
    }

    @Test
    void eliminarSinPermiso() {
        usuarioConRol(RolAcceso.EDITOR);

        assertThatThrownBy(() -> arcoService.eliminar(PROCESO_ID, ARCO_ID, USUARIO_ID))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void desactivarArcosDeElemento() {
        Arco entrada = new Arco(proceso, radicar, revisar, null, null);
        Arco salida = new Arco(proceso, revisar, decision, null, null);
        when(arcoRepository.listarActivosDeElemento(2L)).thenReturn(List.of(entrada, salida));

        List<String> advertencias = arcoService.desactivarArcosDeElemento(2L);

        assertThat(entrada.isActivo()).isFalse();
        assertThat(salida.isActivo()).isFalse();
        assertThat(advertencias).hasSize(2);
    }

    @Test
    void quitarCondicionesDeSalidas() {
        Arco salida = new Arco(proceso, decision, revisar, null, "Si");
        when(arcoRepository.findByOrigenIdAndActivoTrue(3L)).thenReturn(List.of(salida));

        arcoService.quitarCondicionesDeSalidas(3L);

        assertThat(salida.getCondicion()).isNull();
    }

    @Test
    void desactivarArcosDePool() {
        Arco arco = new Arco(proceso, radicar, revisar, null, null);
        when(arcoRepository.listarActivosDePool(10L)).thenReturn(List.of(arco));

        arcoService.desactivarArcosDePool(10L);

        assertThat(arco.isActivo()).isFalse();
    }
}
