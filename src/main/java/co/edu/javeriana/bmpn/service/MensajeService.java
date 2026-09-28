package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.mensaje.CampoMensajeRequest;
import co.edu.javeriana.bmpn.dto.mensaje.CampoMensajeResponse;
import co.edu.javeriana.bmpn.dto.mensaje.CrearMensajeRequest;
import co.edu.javeriana.bmpn.dto.mensaje.MensajeResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.CampoMensaje;
import co.edu.javeriana.bmpn.entity.Evento;
import co.edu.javeriana.bmpn.entity.Mensaje;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.MensajeRepository;

// El sistema modela el intercambio de mensajes; no los envia ni los recibe de verdad
@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioService usuarioService;
    private final ProcesoService procesoService;
    private final PoolService poolService;
    private final EventoService eventoService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public MensajeService(MensajeRepository mensajeRepository,
                          UsuarioService usuarioService,
                          ProcesoService procesoService,
                          PoolService poolService,
                          EventoService eventoService,
                          HistorialProcesoService historialProcesoService,
                          ModelMapper modelMapper) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioService = usuarioService;
        this.procesoService = procesoService;
        this.poolService = poolService;
        this.eventoService = eventoService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    // HU-25 y HU-26
    @Transactional
    public MensajeResponse crear(Long procesoId, Long usuarioId, CrearMensajeRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        Evento envio = eventoService.buscarActivo(request.getEventoEnvioId(), procesoId);
        if (!envio.esEnvioDeMensaje()) {
            throw new SolicitudInvalidaException("El mensaje debe salir de un evento de envio (Message Throw)");
        }

        Evento recepcion = null;
        Pool poolDestino;
        if (request.getEventoRecepcionId() != null) {
            recepcion = eventoService.buscarActivo(request.getEventoRecepcionId(), procesoId);
            if (!recepcion.esRecepcionDeMensaje()) {
                throw new SolicitudInvalidaException("El mensaje debe llegar a un evento de recepcion (Message Catch)");
            }
            poolDestino = recepcion.getPool();
        } else if (request.getPoolDestinoId() != null) {
            poolDestino = poolService.buscarActivo(request.getPoolDestinoId(), procesoId);
        } else {
            throw new SolicitudInvalidaException("Indique el evento que recibe el mensaje o el pool destino");
        }

        // El flujo de mensaje comunica participantes distintos; dentro de un pool se usan arcos
        if (envio.getPool().getId().equals(poolDestino.getId())) {
            throw new SolicitudInvalidaException("Un mensaje debe ir de un pool a otro; dentro del mismo pool se usa un arco");
        }

        String nombre = request.getNombre().trim();
        Mensaje mensaje = new Mensaje(proceso, envio, recepcion, poolDestino, nombre,
                request.getClaveCorrelacion().trim(), request.getPoliticaSinCorrespondencia(),
                request.getPoliticaFallo());
        agregarCampos(mensaje, request.getCampos());
        mensajeRepository.save(mensaje);

        String tipo = mensaje.esNotificacionExterna() ? "Notificacion externa" : "Mensaje";
        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                tipo + " '" + nombre + "' de '" + envio.getPool().getNombre()
                        + "' a '" + poolDestino.getNombre() + "' creado");
        return convertirAResponse(mensaje);
    }

    @Transactional
    public void eliminar(Long procesoId, Long mensajeId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Mensaje mensaje = buscarActivo(mensajeId, procesoId);

        mensaje.desactivar();

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ELIMINACION,
                "Mensaje '" + mensaje.getNombre() + "' eliminado");
    }

    @Transactional(readOnly = true)
    public List<MensajeResponse> listar(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        List<MensajeResponse> respuesta = new ArrayList<>();
        for (Mensaje mensaje : mensajeRepository.listarActivosPorProceso(procesoId)) {
            respuesta.add(convertirAResponse(mensaje));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public MensajeResponse obtener(Long procesoId, Long mensajeId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        return convertirAResponse(buscarActivo(mensajeId, procesoId));
    }

    // HU-28: mensajes del proceso que usan la misma clave de correlacion
    @Transactional(readOnly = true)
    public List<MensajeResponse> listarPorCorrelacion(Long procesoId, String claveCorrelacion, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        List<MensajeResponse> respuesta = new ArrayList<>();
        for (Mensaje mensaje : mensajeRepository.listarPorCorrelacion(procesoId, claveCorrelacion.trim())) {
            respuesta.add(convertirAResponse(mensaje));
        }
        return respuesta;
    }

    private Mensaje buscarActivo(Long mensajeId, Long procesoId) {
        return mensajeRepository.findByIdAndProcesoIdAndActivoTrue(mensajeId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mensaje no encontrado en el proceso"));
    }

    // HU-25: se documentan los datos que viajan en el mensaje
    private void agregarCampos(Mensaje mensaje, List<CampoMensajeRequest> campos) {
        List<String> nombres = new ArrayList<>();
        for (CampoMensajeRequest campo : campos) {
            String nombre = campo.getNombre().trim();
            if (nombres.contains(nombre.toLowerCase())) {
                throw new SolicitudInvalidaException("El campo '" + nombre + "' esta repetido en el mensaje");
            }
            nombres.add(nombre.toLowerCase());
            mensaje.agregarCampo(new CampoMensaje(nombre, campo.getTipoDato().trim(),
                    campo.getDescripcion(), campo.isObligatorio()));
        }
    }

    // Reglas que se muestran como advertencias porque el diagrama puede estar incompleto
    private List<String> revisarCoherencia(Mensaje mensaje) {
        List<String> advertencias = new ArrayList<>();
        Evento recepcion = mensaje.getEventoRecepcion();

        // HU-25: el mensaje necesita un Message Catch en el pool destino con su mismo nombre
        if (recepcion == null && !mensaje.getPoolDestino().isCajaNegra()) {
            advertencias.add("El mensaje '" + mensaje.getNombre() + "' queda sin receptor en el pool destino");
        }
        if (recepcion != null && !recepcion.getNombre().equalsIgnoreCase(mensaje.getNombre())) {
            advertencias.add("El mensaje '" + mensaje.getNombre()
                    + "' no coincide con el nombre del evento que lo recibe ('" + recepcion.getNombre() + "')");
        }

        // HU-26: la notificacion externa documenta el canal y que pasa si falla
        if (mensaje.esNotificacionExterna()) {
            if (mensaje.getPoolDestino().getCanalExterno() == null) {
                advertencias.add("Indique en el pool destino el canal: correo, servicio web o cola de mensajes");
            }
            if (mensaje.getPoliticaFallo() == null) {
                advertencias.add("Indique que ocurre en el modelo si la notificacion externa falla");
            }
        }

        // HU-28: dos mensajes con el mismo nombre y la misma clave no se pueden distinguir
        if (mensajeRepository.existsByProcesoIdAndNombreIgnoreCaseAndClaveCorrelacionIgnoreCaseAndActivoTrueAndIdNot(
                mensaje.getProceso().getId(), mensaje.getNombre(), mensaje.getClaveCorrelacion(), mensaje.getId())) {
            advertencias.add("Hay otro mensaje con el nombre '" + mensaje.getNombre()
                    + "' y la clave '" + mensaje.getClaveCorrelacion() + "'; son ambiguos");
        }
        if (mensaje.getPoliticaSinCorrespondencia() == null) {
            advertencias.add("Indique que pasa si el mensaje no corresponde a ningun caso: descartarlo o iniciar uno nuevo");
        }
        return advertencias;
    }

    private MensajeResponse convertirAResponse(Mensaje mensaje) {
        MensajeResponse respuesta = modelMapper.map(mensaje, MensajeResponse.class);
        if (mensaje.getEventoRecepcion() != null) {
            respuesta.setEventoRecepcionId(mensaje.getEventoRecepcion().getId());
        }
        respuesta.setNotificacionExterna(mensaje.esNotificacionExterna());
        respuesta.setCanalDestino(mensaje.getPoolDestino().getCanalExterno());
        List<CampoMensajeResponse> campos = new ArrayList<>();
        for (CampoMensaje campo : mensaje.getCampos()) {
            campos.add(modelMapper.map(campo, CampoMensajeResponse.class));
        }
        respuesta.setCampos(campos);
        respuesta.setAdvertencias(revisarCoherencia(mensaje));
        return respuesta;
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar mensajes");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar mensajes");
        }
    }
}
