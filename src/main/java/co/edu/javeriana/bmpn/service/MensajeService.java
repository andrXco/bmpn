package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.mensaje.CrearMensajeRequest;
import co.edu.javeriana.bmpn.dto.mensaje.MensajeResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Mensaje;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.MensajeRepository;

@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioService usuarioService;
    private final ProcesoService procesoService;
    private final PoolService poolService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public MensajeService(MensajeRepository mensajeRepository,
                          UsuarioService usuarioService,
                          ProcesoService procesoService,
                          PoolService poolService,
                          HistorialProcesoService historialProcesoService,
                          ModelMapper modelMapper) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioService = usuarioService;
        this.procesoService = procesoService;
        this.poolService = poolService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    //25 (Message Throw entre pools del mismo proceso)
    //26 (notificacion a un pool SISTEMA_EXTERNO)
    @Transactional
    public MensajeResponse enviar(Long procesoId, Long usuarioId, CrearMensajeRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        if (request.getPoolOrigenId().equals(request.getPoolDestinoId())) {
            throw new SolicitudInvalidaException("Un mensaje de colaboracion debe ser entre pools distintos");
        }
        Pool origen = poolService.buscarActivo(request.getPoolOrigenId(), procesoId);
        Pool destino = poolService.buscarActivo(request.getPoolDestinoId(), procesoId);

        String nombre = request.getNombre().trim();
        String claveCorrelacion = request.getClaveCorrelacion().trim();
        Mensaje mensaje = new Mensaje(proceso, origen, destino, nombre, claveCorrelacion,
                request.getPoliticaSinCorrespondencia(), request.getPoliticaFallo());
        mensajeRepository.save(mensaje);

        String tipo = mensaje.esNotificacionExterna() ? "Notificacion externa" : "Mensaje de colaboracion";
        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                tipo + " '" + nombre + "' enviado de '" + origen.getNombre() + "' a '" + destino.getNombre() + "'");
        return convertirAResponse(mensaje);
    }

    //27: al recibir el mensaje se activa (publica) el proceso si esta en borrador y no lo ejecuta
    @Transactional
    public MensajeResponse recibir(Long procesoId, Long mensajeId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Mensaje mensaje = buscarActivo(mensajeId, procesoId);

        proceso.activar();

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                "Mensaje '" + mensaje.getNombre() + "' recibido; proceso en estado " + proceso.getEstado());
        return convertirAResponse(mensaje);
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

    //28 mensajes que comparten clave de correlacion dentro del mismo proceso
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

    private MensajeResponse convertirAResponse(Mensaje mensaje) {
        return modelMapper.map(mensaje, MensajeResponse.class);
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede gestionar mensajes");
        }
    }
}