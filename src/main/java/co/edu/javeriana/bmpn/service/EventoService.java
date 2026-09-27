package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
import co.edu.javeriana.bmpn.dto.evento.CrearEventoRequest;
import co.edu.javeriana.bmpn.dto.evento.EditarEventoRequest;
import co.edu.javeriana.bmpn.dto.evento.EventoResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.DisparadorEvento;
import co.edu.javeriana.bmpn.entity.Evento;
import co.edu.javeriana.bmpn.entity.Lane;
import co.edu.javeriana.bmpn.entity.Mensaje;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoEvento;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.EventoRepository;

// Eventos de inicio, intermedios y de fin; los de mensaje son el Message Throw y el Message Catch
@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioService usuarioService;
    private final ProcesoService procesoService;
    private final ElementoProcesoService elementoProcesoService;
    private final LaneService laneService;
    private final ArcoService arcoService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public EventoService(EventoRepository eventoRepository,
                         UsuarioService usuarioService,
                         ProcesoService procesoService,
                         ElementoProcesoService elementoProcesoService,
                         LaneService laneService,
                         ArcoService arcoService,
                         HistorialProcesoService historialProcesoService,
                         ModelMapper modelMapper) {
        this.eventoRepository = eventoRepository;
        this.usuarioService = usuarioService;
        this.procesoService = procesoService;
        this.elementoProcesoService = elementoProcesoService;
        this.laneService = laneService;
        this.arcoService = arcoService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public EventoResponse crear(Long procesoId, Long usuarioId, CrearEventoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        validarCombinacion(request.getTipoEvento(), request.getDisparador(), request.isOrigenExterno());
        Pool pool = elementoProcesoService.buscarPoolParaElemento(proceso, request.getPoolId());

        String nombre = request.getNombre().trim();
        Evento evento = new Evento(proceso, pool, nombre, request.getTipoEvento(), request.getDisparador(),
                request.isOrigenExterno(), request.getPosicionX(), request.getPosicionY());
        if (request.getLaneId() != null) {
            Lane lane = laneService.buscarActiva(request.getLaneId(), pool.getId());
            evento.asignarLane(lane);
        }
        eventoRepository.save(evento);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                "Evento '" + nombre + "' de tipo " + evento.getTipoEvento() + " creado");
        return convertirAResponse(evento);
    }

    @Transactional
    public EventoResponse editar(Long procesoId, Long eventoId, Long usuarioId, EditarEventoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Evento evento = buscarActivo(eventoId, procesoId);

        validarCombinacion(request.getTipoEvento(), request.getDisparador(), request.isOrigenExterno());
        // Al volverlo de inicio o de fin, sus arcos actuales dejarian de ser validos
        if (request.getTipoEvento() == TipoEvento.INICIO && arcoService.contarEntradasActivas(eventoId) > 0) {
            throw new SolicitudInvalidaException("El evento tiene arcos de entrada; no puede ser de inicio");
        }
        if (request.getTipoEvento() == TipoEvento.FIN && !arcoService.listarSalidasActivas(eventoId).isEmpty()) {
            throw new SolicitudInvalidaException("El evento tiene arcos de salida; no puede ser de fin");
        }

        String nombre = request.getNombre().trim();
        evento.renombrar(nombre);
        evento.actualizar(request.getTipoEvento(), request.getDisparador(), request.isOrigenExterno());
        evento.mover(request.getPosicionX(), request.getPosicionY());
        if (request.getLaneId() != null) {
            evento.asignarLane(laneService.buscarActiva(request.getLaneId(), evento.getPool().getId()));
        }

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                "Evento '" + nombre + "' actualizado");
        return convertirAResponse(evento);
    }

    // La eliminacion es logica y arrastra los arcos del evento
    @Transactional
    public AdvertenciasResponse eliminar(Long procesoId, Long eventoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Evento evento = buscarActivo(eventoId, procesoId);

        evento.desactivar();
        List<String> advertencias = new ArrayList<>(arcoService.desactivarArcosDeElemento(eventoId));
        // Un mensaje no puede quedar saliendo o llegando a un evento eliminado
        for (Mensaje mensaje : evento.getMensajesEnviados()) {
            desactivarMensaje(mensaje, advertencias);
        }
        for (Mensaje mensaje : evento.getMensajesRecibidos()) {
            desactivarMensaje(mensaje, advertencias);
        }

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ELIMINACION,
                "Evento '" + evento.getNombre() + "' eliminado");
        return new AdvertenciasResponse(advertencias);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listar(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        List<EventoResponse> respuesta = new ArrayList<>();
        for (Evento evento : eventoRepository.listarActivosPorProceso(procesoId)) {
            respuesta.add(convertirAResponse(evento));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public EventoResponse obtener(Long procesoId, Long eventoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        return convertirAResponse(buscarActivo(eventoId, procesoId));
    }

    // Para MensajeService: el mensaje sale de un evento de envio y llega a uno de recepcion
    public Evento buscarActivo(Long eventoId, Long procesoId) {
        return eventoRepository.findByIdAndProcesoIdAndActivoTrue(eventoId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado en el proceso"));
    }

    private void desactivarMensaje(Mensaje mensaje, List<String> advertencias) {
        if (mensaje.isActivo()) {
            mensaje.desactivar();
            advertencias.add("El mensaje '" + mensaje.getNombre() + "' se elimino junto con el evento");
        }
    }

    private void validarCombinacion(TipoEvento tipo, DisparadorEvento disparador, boolean origenExterno) {
        if (tipo == TipoEvento.INICIO && disparador == DisparadorEvento.MENSAJE_ENVIO) {
            throw new SolicitudInvalidaException("Un evento de inicio no puede enviar mensajes");
        }
        if (tipo == TipoEvento.FIN && disparador == DisparadorEvento.MENSAJE_RECEPCION) {
            throw new SolicitudInvalidaException("Un evento de fin no puede esperar mensajes");
        }
        if (origenExterno && disparador != DisparadorEvento.MENSAJE_RECEPCION) {
            throw new SolicitudInvalidaException("Solo un evento que recibe mensajes puede ser de origen externo");
        }
    }

    // HU-25 y HU-27: un Throw sin mensaje, o un Catch que nadie alimenta, dejan el diagrama incompleto
    private List<String> revisarMensajes(Evento evento) {
        List<String> advertencias = new ArrayList<>();
        if (evento.esEnvioDeMensaje() && !evento.enviaAlgunMensaje()) {
            advertencias.add("El evento '" + evento.getNombre() + "' no envia ningun mensaje");
        }
        if (evento.esRecepcionDeMensaje() && !evento.isOrigenExterno() && !evento.recibeAlgunMensaje()) {
            advertencias.add("El evento '" + evento.getNombre()
                    + "' espera un mensaje que nadie envia; relacionelo con un Message Throw o marquelo de origen externo");
        }
        return advertencias;
    }

    private EventoResponse convertirAResponse(Evento evento) {
        EventoResponse respuesta = modelMapper.map(evento, EventoResponse.class);
        if (evento.getLane() != null) {
            respuesta.setLaneId(evento.getLane().getId());
        }
        respuesta.setAdvertencias(revisarMensajes(evento));
        return respuesta;
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar eventos");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar eventos");
        }
    }
}
