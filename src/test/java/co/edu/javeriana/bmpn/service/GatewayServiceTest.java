package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
import co.edu.javeriana.bmpn.dto.gateway.CrearGatewayRequest;
import co.edu.javeriana.bmpn.dto.gateway.EditarGatewayRequest;
import co.edu.javeriana.bmpn.dto.gateway.GatewayResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Actividad;
import co.edu.javeriana.bmpn.entity.Arco;
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
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.GatewayRepository;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long USUARIO_ID = 5L;
    private static final Long GATEWAY_ID = 3L;

    @Mock
    private GatewayRepository gatewayRepository;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ElementoProcesoService elementoProcesoService;

    @Mock
    private ArcoService arcoService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    private GatewayService gatewayService;

    private Empresa empresa;
    private Proceso proceso;
    private Pool pool;
    private Gateway gateway;
    private Actividad aprobar;
    private Actividad rechazar;

    @BeforeEach
    void prepararDatos() {
        gatewayService = new GatewayService(gatewayRepository, procesoService, usuarioService,
                elementoProcesoService, arcoService, historialProcesoService,
                new ModelMapperConfig().modelMapper());

        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        proceso = new Proceso(empresa, "Solicitud de vacaciones", "Proceso de ejemplo", "RRHH");
        pool = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        gateway = new Gateway(proceso, pool, "Aprobada?", TipoGateway.EXCLUSIVO, BigDecimal.ONE, BigDecimal.ONE);
        aprobar = new Actividad(proceso, pool, "Aprobar", TipoActividad.USUARIO, BigDecimal.ONE, BigDecimal.ONE);
        rechazar = new Actividad(proceso, pool, "Rechazar", TipoActividad.USUARIO, BigDecimal.ONE, BigDecimal.ONE);
        // En las pruebas no hay base de datos, asi que el id se asigna a mano
        ReflectionTestUtils.setField(gateway, "id", GATEWAY_ID);
    }

    private Usuario usuarioConRol(RolAcceso rol) {
        Usuario usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", rol);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
        return usuario;
    }

    private void gatewayEncontrado() {
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(gatewayRepository.findByIdAndProcesoIdAndActivoTrue(GATEWAY_ID, PROCESO_ID))
                .thenReturn(Optional.of(gateway));
    }

    private void salidas(Arco... arcos) {
        when(arcoService.listarSalidasActivas(GATEWAY_ID)).thenReturn(List.of(arcos));
    }

    @Test
    void crearGateway() {
        Usuario editor = usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(elementoProcesoService.buscarPoolParaElemento(eq(proceso), isNull())).thenReturn(pool);

        GatewayResponse respuesta = gatewayService.crear(PROCESO_ID, USUARIO_ID,
                new CrearGatewayRequest(" Aprobada? ", TipoGateway.EXCLUSIVO, BigDecimal.ONE, BigDecimal.ONE, null));

        assertThat(respuesta.getNombre()).isEqualTo("Aprobada?");
        assertThat(respuesta.getTipoGateway()).isEqualTo(TipoGateway.EXCLUSIVO);
        // Recien creado no tiene salidas, asi que avisa que le faltan
        assertThat(respuesta.getAdvertencias()).hasSize(1);
        verify(gatewayRepository).save(any(Gateway.class));
        verify(historialProcesoService).registrar(eq(proceso), eq(editor),
                eq(AccionHistorial.CREACION), anyString());
    }

    @Test
    void crearSinPermiso() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);

        assertThatThrownBy(() -> gatewayService.crear(PROCESO_ID, USUARIO_ID,
                new CrearGatewayRequest("Aprobada?", TipoGateway.EXCLUSIVO, BigDecimal.ONE, BigDecimal.ONE, null)))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(gatewayRepository, never()).save(any());
    }

    @Test
    void exclusivoCompletoSinAdvertencias() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        gatewayEncontrado();
        salidas(new Arco(proceso, gateway, aprobar, null, "Si"),
                new Arco(proceso, gateway, rechazar, null, "No"));

        GatewayResponse respuesta = gatewayService.obtener(PROCESO_ID, GATEWAY_ID, USUARIO_ID);

        assertThat(respuesta.getAdvertencias()).isEmpty();
    }

    @Test
    void advierteSalidaSinCondicion() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        gatewayEncontrado();
        salidas(new Arco(proceso, gateway, aprobar, null, "Si"),
                new Arco(proceso, gateway, rechazar, null, null));

        GatewayResponse respuesta = gatewayService.obtener(PROCESO_ID, GATEWAY_ID, USUARIO_ID);

        assertThat(respuesta.getAdvertencias()).hasSize(1);
        assertThat(respuesta.getAdvertencias().get(0)).contains("Rechazar");
    }

    @Test
    void advierteCondicionesRepetidasEnExclusivo() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        gatewayEncontrado();
        salidas(new Arco(proceso, gateway, aprobar, null, "Si"),
                new Arco(proceso, gateway, rechazar, null, "si"));

        GatewayResponse respuesta = gatewayService.obtener(PROCESO_ID, GATEWAY_ID, USUARIO_ID);

        assertThat(respuesta.getAdvertencias()).hasSize(1);
        assertThat(respuesta.getAdvertencias().get(0)).contains("misma condicion");
    }

    @Test
    void gatewayQueJuntaCaminosNoAdvierte() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        gatewayEncontrado();
        salidas(new Arco(proceso, gateway, aprobar, null, null));
        when(arcoService.contarEntradasActivas(GATEWAY_ID)).thenReturn(2L);

        GatewayResponse respuesta = gatewayService.obtener(PROCESO_ID, GATEWAY_ID, USUARIO_ID);

        assertThat(respuesta.getAdvertencias()).isEmpty();
    }

    @Test
    void editarAParaleloQuitaCondiciones() {
        usuarioConRol(RolAcceso.EDITOR);
        gatewayEncontrado();

        GatewayResponse respuesta = gatewayService.editar(PROCESO_ID, GATEWAY_ID, USUARIO_ID,
                new EditarGatewayRequest("Aprobada?", TipoGateway.PARALELO, BigDecimal.ONE, BigDecimal.ONE));

        assertThat(respuesta.getTipoGateway()).isEqualTo(TipoGateway.PARALELO);
        verify(arcoService).quitarCondicionesDeSalidas(GATEWAY_ID);
    }

    @Test
    void editarSinCambiarTipoConservaCondiciones() {
        usuarioConRol(RolAcceso.EDITOR);
        gatewayEncontrado();

        gatewayService.editar(PROCESO_ID, GATEWAY_ID, USUARIO_ID,
                new EditarGatewayRequest("Decision", TipoGateway.EXCLUSIVO, BigDecimal.TEN, BigDecimal.TEN));

        assertThat(gateway.getNombre()).isEqualTo("Decision");
        verify(arcoService, never()).quitarCondicionesDeSalidas(anyLong());
    }

    @Test
    void editarGatewayInexistente() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(gatewayRepository.findByIdAndProcesoIdAndActivoTrue(GATEWAY_ID, PROCESO_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> gatewayService.editar(PROCESO_ID, GATEWAY_ID, USUARIO_ID,
                new EditarGatewayRequest("Decision", TipoGateway.EXCLUSIVO, BigDecimal.ONE, BigDecimal.ONE)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarGatewayConRamas() {
        Usuario administrador = usuarioConRol(RolAcceso.ADMINISTRADOR);
        gatewayEncontrado();
        salidas(new Arco(proceso, gateway, aprobar, null, "Si"),
                new Arco(proceso, gateway, rechazar, null, "No"));
        when(arcoService.desactivarArcosDeElemento(GATEWAY_ID))
                .thenReturn(List.of("El elemento 'Aprobar' quedo sin camino de entrada"));

        AdvertenciasResponse respuesta = gatewayService.eliminar(PROCESO_ID, GATEWAY_ID, USUARIO_ID);

        assertThat(gateway.isActivo()).isFalse();
        assertThat(respuesta.getAdvertencias()).hasSize(2);
        assertThat(respuesta.getAdvertencias().get(0)).contains("sin punto de decision");
        verify(historialProcesoService).registrar(eq(proceso), eq(administrador),
                eq(AccionHistorial.ELIMINACION), anyString());
    }

    @Test
    void eliminarSinPermiso() {
        usuarioConRol(RolAcceso.EDITOR);

        assertThatThrownBy(() -> gatewayService.eliminar(PROCESO_ID, GATEWAY_ID, USUARIO_ID))
                .isInstanceOf(AccesoDenegadoException.class);
    }
}
