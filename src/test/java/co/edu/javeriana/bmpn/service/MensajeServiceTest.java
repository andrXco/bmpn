package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import co.edu.javeriana.bmpn.dto.mensaje.CampoMensajeRequest;
import co.edu.javeriana.bmpn.dto.mensaje.CrearMensajeRequest;
import co.edu.javeriana.bmpn.dto.mensaje.MensajeResponse;
import co.edu.javeriana.bmpn.entity.DisparadorEvento;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Evento;
import co.edu.javeriana.bmpn.entity.Mensaje;
import co.edu.javeriana.bmpn.entity.PoliticaFallo;
import co.edu.javeriana.bmpn.entity.PoliticaSinCorrespondencia;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoEvento;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.MensajeRepository;

@ExtendWith(MockitoExtension.class)
class MensajeServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long USUARIO_ID = 5L;
    private static final Long THROW_ID = 30L;
    private static final Long CATCH_ID = 31L;
    private static final Long POOL_CORREO_ID = 12L;

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private PoolService poolService;

    @Mock
    private EventoService eventoService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    private MensajeService mensajeService;

    private Empresa empresa;
    private Proceso proceso;
    private Pool poolEmpresa;
    private Pool poolBanco;
    private Pool poolCorreo;
    private Evento envio;
    private Evento recepcion;

    @BeforeEach
    void prepararDatos() {
        mensajeService = new MensajeService(mensajeRepository, usuarioService, procesoService,
                poolService, eventoService, historialProcesoService, new ModelMapperConfig().modelMapper());

        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        proceso = new Proceso(empresa, "Vacaciones", "Solicitud", "RRHH");
        poolEmpresa = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        poolBanco = new Pool(empresa, "Banco", TipoParticipante.PROVEEDOR, 1);
        poolCorreo = new Pool(null, "Servicio de correo", TipoParticipante.SISTEMA_EXTERNO, 2, "CORREO", true);
        // En las pruebas no hay base de datos, asi que el id se asigna a mano
        ReflectionTestUtils.setField(poolEmpresa, "id", 10L);
        ReflectionTestUtils.setField(poolBanco, "id", 11L);
        ReflectionTestUtils.setField(poolCorreo, "id", POOL_CORREO_ID);

        envio = new Evento(proceso, poolEmpresa, "Pago solicitado", TipoEvento.INTERMEDIO,
                DisparadorEvento.MENSAJE_ENVIO, false, BigDecimal.ONE, BigDecimal.ONE);
        recepcion = new Evento(proceso, poolBanco, "Pago solicitado", TipoEvento.INICIO,
                DisparadorEvento.MENSAJE_RECEPCION, false, BigDecimal.ONE, BigDecimal.ONE);
    }

    private void usuarioYProceso() {
        Usuario usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", RolAcceso.EDITOR);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
    }

    private CrearMensajeRequest solicitud(Long eventoRecepcionId, Long poolDestinoId) {
        CrearMensajeRequest request = new CrearMensajeRequest();
        request.setEventoEnvioId(THROW_ID);
        request.setEventoRecepcionId(eventoRecepcionId);
        request.setPoolDestinoId(poolDestinoId);
        request.setNombre("Pago solicitado");
        request.setClaveCorrelacion("numero de solicitud");
        request.setPoliticaSinCorrespondencia(PoliticaSinCorrespondencia.DESCARTAR);
        return request;
    }

    private CampoMensajeRequest campo(String nombre, String tipoDato) {
        CampoMensajeRequest campo = new CampoMensajeRequest();
        campo.setNombre(nombre);
        campo.setTipoDato(tipoDato);
        return campo;
    }

    @Test
    void crearMensajeDeThrowACatchConCampos() {
        usuarioYProceso();
        when(eventoService.buscarActivo(THROW_ID, PROCESO_ID)).thenReturn(envio);
        when(eventoService.buscarActivo(CATCH_ID, PROCESO_ID)).thenReturn(recepcion);
        CrearMensajeRequest request = solicitud(CATCH_ID, null);
        request.setCampos(List.of(campo("numeroSolicitud", "TEXTO"), campo("valor", "DECIMAL")));

        MensajeResponse respuesta = mensajeService.crear(PROCESO_ID, USUARIO_ID, request);

        assertThat(respuesta.getCampos()).hasSize(2);
        assertThat(respuesta.getPoolDestinoId()).isEqualTo(11L);
        assertThat(respuesta.getAdvertencias()).isEmpty();
        verify(mensajeRepository).save(any(Mensaje.class));
    }

    @Test
    void elMensajeDebeSalirDeUnThrow() {
        usuarioYProceso();
        when(eventoService.buscarActivo(THROW_ID, PROCESO_ID)).thenReturn(recepcion);

        assertThatThrownBy(() -> mensajeService.crear(PROCESO_ID, USUARIO_ID, solicitud(CATCH_ID, null)))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(mensajeRepository, never()).save(any());
    }

    @Test
    void noSeEnviaDentroDelMismoPool() {
        usuarioYProceso();
        when(eventoService.buscarActivo(THROW_ID, PROCESO_ID)).thenReturn(envio);
        when(poolService.buscarActivo(10L, PROCESO_ID)).thenReturn(poolEmpresa);

        assertThatThrownBy(() -> mensajeService.crear(PROCESO_ID, USUARIO_ID, solicitud(null, 10L)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void camposRepetidos() {
        usuarioYProceso();
        when(eventoService.buscarActivo(THROW_ID, PROCESO_ID)).thenReturn(envio);
        when(eventoService.buscarActivo(CATCH_ID, PROCESO_ID)).thenReturn(recepcion);
        CrearMensajeRequest request = solicitud(CATCH_ID, null);
        request.setCampos(List.of(campo("valor", "DECIMAL"), campo("Valor", "TEXTO")));

        assertThatThrownBy(() -> mensajeService.crear(PROCESO_ID, USUARIO_ID, request))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void notificacionExternaSinPoliticaDeFalloAdvierte() {
        usuarioYProceso();
        when(eventoService.buscarActivo(THROW_ID, PROCESO_ID)).thenReturn(envio);
        when(poolService.buscarActivo(POOL_CORREO_ID, PROCESO_ID)).thenReturn(poolCorreo);

        MensajeResponse respuesta = mensajeService.crear(PROCESO_ID, USUARIO_ID, solicitud(null, POOL_CORREO_ID));

        assertThat(respuesta.isNotificacionExterna()).isTrue();
        assertThat(respuesta.getCanalDestino()).isEqualTo("CORREO");
        assertThat(respuesta.getAdvertencias()).hasSize(1);
        assertThat(respuesta.getAdvertencias().get(0)).contains("falla");
    }

    @Test
    void notificacionExternaCompletaSinAdvertencias() {
        usuarioYProceso();
        when(eventoService.buscarActivo(THROW_ID, PROCESO_ID)).thenReturn(envio);
        when(poolService.buscarActivo(POOL_CORREO_ID, PROCESO_ID)).thenReturn(poolCorreo);
        CrearMensajeRequest request = solicitud(null, POOL_CORREO_ID);
        request.setPoliticaFallo(PoliticaFallo.CONTINUAR);

        MensajeResponse respuesta = mensajeService.crear(PROCESO_ID, USUARIO_ID, request);

        assertThat(respuesta.getAdvertencias()).isEmpty();
    }

    @Test
    void mensajesConMismoNombreYClaveSonAmbiguos() {
        usuarioYProceso();
        when(eventoService.buscarActivo(THROW_ID, PROCESO_ID)).thenReturn(envio);
        when(eventoService.buscarActivo(CATCH_ID, PROCESO_ID)).thenReturn(recepcion);
        when(mensajeRepository.existsByProcesoIdAndNombreIgnoreCaseAndClaveCorrelacionIgnoreCaseAndActivoTrueAndIdNot(
                any(), anyString(), anyString(), any())).thenReturn(true);

        MensajeResponse respuesta = mensajeService.crear(PROCESO_ID, USUARIO_ID, solicitud(CATCH_ID, null));

        assertThat(respuesta.getAdvertencias()).hasSize(1);
        assertThat(respuesta.getAdvertencias().get(0)).contains("ambiguos");
    }

    @Test
    void sinReceptorNiPoolDestino() {
        usuarioYProceso();
        when(eventoService.buscarActivo(THROW_ID, PROCESO_ID)).thenReturn(envio);

        assertThatThrownBy(() -> mensajeService.crear(PROCESO_ID, USUARIO_ID, solicitud(null, null)))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(poolService, never()).buscarActivo(anyLong(), anyLong());
    }

    @Test
    void eliminarMensaje() {
        Usuario administrador = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", RolAcceso.ADMINISTRADOR);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(administrador);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        Mensaje mensaje = new Mensaje(proceso, envio, recepcion, poolBanco, "Pago solicitado", "numero", null, null);
        when(mensajeRepository.findByIdAndProcesoIdAndActivoTrue(40L, PROCESO_ID)).thenReturn(Optional.of(mensaje));

        mensajeService.eliminar(PROCESO_ID, 40L, USUARIO_ID);

        assertThat(mensaje.isActivo()).isFalse();
    }

    @Test
    void listarPorCorrelacion() {
        usuarioYProceso();
        Mensaje mensaje = new Mensaje(proceso, envio, recepcion, poolBanco, "Pago solicitado", "numero",
                PoliticaSinCorrespondencia.DESCARTAR, null);
        when(mensajeRepository.listarPorCorrelacion(PROCESO_ID, "numero")).thenReturn(List.of(mensaje));

        List<MensajeResponse> mensajes = mensajeService.listarPorCorrelacion(PROCESO_ID, " numero ", USUARIO_ID);

        assertThat(mensajes).hasSize(1);
        assertThat(mensajes.get(0).getClaveCorrelacion()).isEqualTo("numero");
    }
}
