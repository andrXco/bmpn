package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
import co.edu.javeriana.bmpn.dto.evento.CrearEventoRequest;
import co.edu.javeriana.bmpn.dto.evento.EditarEventoRequest;
import co.edu.javeriana.bmpn.dto.evento.EventoResponse;
import co.edu.javeriana.bmpn.entity.DisparadorEvento;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Evento;
import co.edu.javeriana.bmpn.entity.Mensaje;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoEvento;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.EventoRepository;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long EVENTO_ID = 30L;
    private static final Long USUARIO_ID = 5L;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private ElementoProcesoService elementoProcesoService;

    @Mock
    private LaneService laneService;

    @Mock
    private ArcoService arcoService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    private EventoService eventoService;

    private Empresa empresa;
    private Proceso proceso;
    private Pool pool;

    @BeforeEach
    void prepararDatos() {
        eventoService = new EventoService(eventoRepository, usuarioService, procesoService,
                elementoProcesoService, laneService, arcoService, historialProcesoService,
                new ModelMapperConfig().modelMapper());

        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        proceso = new Proceso(empresa, "Vacaciones", "Solicitud", "RRHH");
        pool = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        proceso.agregarPool(pool);
    }

    private void usuarioConRol(RolAcceso rolAcceso) {
        Usuario usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", rolAcceso);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
    }

    private CrearEventoRequest solicitud(TipoEvento tipo, DisparadorEvento disparador) {
        CrearEventoRequest request = new CrearEventoRequest();
        request.setNombre("Pago solicitado");
        request.setTipoEvento(tipo);
        request.setDisparador(disparador);
        request.setPosicionX(BigDecimal.ONE);
        request.setPosicionY(BigDecimal.ONE);
        return request;
    }

    private Evento eventoExistente(TipoEvento tipo, DisparadorEvento disparador) {
        Evento evento = new Evento(proceso, pool, "Pago solicitado", tipo, disparador, false,
                BigDecimal.ONE, BigDecimal.ONE);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(eventoRepository.findByIdAndProcesoIdAndActivoTrue(EVENTO_ID, PROCESO_ID)).thenReturn(Optional.of(evento));
        return evento;
    }

    @Test
    void crearEventoDeEnvioAdvierteQueNoEnviaMensajes() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        when(elementoProcesoService.buscarPoolParaElemento(eq(proceso), isNull())).thenReturn(pool);

        EventoResponse respuesta = eventoService.crear(PROCESO_ID, USUARIO_ID,
                solicitud(TipoEvento.INTERMEDIO, DisparadorEvento.MENSAJE_ENVIO));

        assertThat(respuesta.getDisparador()).isEqualTo(DisparadorEvento.MENSAJE_ENVIO);
        assertThat(respuesta.getAdvertencias()).hasSize(1);
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void inicioNoPuedeEnviarMensajes() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);

        CrearEventoRequest solicitud = solicitud(TipoEvento.INICIO, DisparadorEvento.MENSAJE_ENVIO);
        assertThatThrownBy(() -> eventoService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void finNoPuedeEsperarMensajes() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);

        CrearEventoRequest solicitud = solicitud(TipoEvento.FIN, DisparadorEvento.MENSAJE_RECEPCION);
        assertThatThrownBy(() -> eventoService.crear(PROCESO_ID, USUARIO_ID, solicitud))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void soloLaRecepcionPuedeSerDeOrigenExterno() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
        CrearEventoRequest request = solicitud(TipoEvento.INTERMEDIO, DisparadorEvento.NINGUNO);
        request.setOrigenExterno(true);

        assertThatThrownBy(() -> eventoService.crear(PROCESO_ID, USUARIO_ID, request))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void recepcionSinMensajeAdvierte() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        eventoExistente(TipoEvento.INICIO, DisparadorEvento.MENSAJE_RECEPCION);

        EventoResponse respuesta = eventoService.obtener(PROCESO_ID, EVENTO_ID, USUARIO_ID);

        assertThat(respuesta.getAdvertencias()).hasSize(1);
        assertThat(respuesta.getAdvertencias().get(0)).contains("nadie envia");
    }

    @Test
    void noSePuedeVolverInicioSiTieneEntradas() {
        usuarioConRol(RolAcceso.EDITOR);
        eventoExistente(TipoEvento.INTERMEDIO, DisparadorEvento.NINGUNO);
        when(arcoService.contarEntradasActivas(EVENTO_ID)).thenReturn(1L);
        EditarEventoRequest request = new EditarEventoRequest();
        request.setNombre("Inicio");
        request.setTipoEvento(TipoEvento.INICIO);
        request.setDisparador(DisparadorEvento.NINGUNO);
        request.setPosicionX(BigDecimal.ONE);
        request.setPosicionY(BigDecimal.ONE);

        assertThatThrownBy(() -> eventoService.editar(PROCESO_ID, EVENTO_ID, USUARIO_ID, request))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void eliminarEventoDesactivaSusMensajes() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);
        Evento envio = eventoExistente(TipoEvento.INTERMEDIO, DisparadorEvento.MENSAJE_ENVIO);
        Pool externo = new Pool(null, "Banco", TipoParticipante.SISTEMA_EXTERNO, 1);
        Mensaje mensaje = new Mensaje(proceso, envio, null, externo, "Pago solicitado", "numero", null, null);
        envio.getMensajesEnviados().add(mensaje);
        when(arcoService.desactivarArcosDeElemento(EVENTO_ID)).thenReturn(List.of());

        AdvertenciasResponse respuesta = eventoService.eliminar(PROCESO_ID, EVENTO_ID, USUARIO_ID);

        assertThat(envio.isActivo()).isFalse();
        assertThat(mensaje.isActivo()).isFalse();
        assertThat(respuesta.getAdvertencias()).hasSize(1);
    }

    @Test
    void eliminarSinPermiso() {
        usuarioConRol(RolAcceso.EDITOR);

        assertThatThrownBy(() -> eventoService.eliminar(PROCESO_ID, EVENTO_ID, USUARIO_ID))
                .isInstanceOf(AccesoDenegadoException.class);
    }
}
