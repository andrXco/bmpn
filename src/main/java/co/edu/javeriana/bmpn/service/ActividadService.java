package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.actividad.ActividadResponse;
import co.edu.javeriana.bmpn.dto.actividad.CrearActividadRequest;
import co.edu.javeriana.bmpn.dto.actividad.EditarActividadRequest;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Actividad;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.ActividadRepository;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final ProcesoService procesoService;
    private final UsuarioService usuarioService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public ActividadService(ActividadRepository actividadRepository,
                            ProcesoService procesoService,
                            UsuarioService usuarioService,
                            HistorialProcesoService historialProcesoService,
                            ModelMapper modelMapper) {
        this.actividadRepository = actividadRepository;
        this.procesoService = procesoService;
        this.usuarioService = usuarioService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    // HU-08
    @Transactional
    public ActividadResponse crear(Long procesoId, Long usuarioId, CrearActividadRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        String nombre = request.getNombre().trim();
        if (actividadRepository.existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(procesoId, nombre)) {
            throw new RecursoDuplicadoException("Ya existe una actividad con ese nombre en el proceso");
        }

        Pool pool = buscarPoolDelProceso(proceso, request.getPoolId());
        Actividad actividad = new Actividad(proceso, pool, nombre, request.getTipoActividad(),
                request.getPosicionX(), request.getPosicionY());
        actividadRepository.save(actividad);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                "Actividad '" + nombre + "' creada");
        return convertirAResponse(actividad);
    }

    // HU-09
    @Transactional
    public ActividadResponse editar(Long procesoId, Long actividadId, Long usuarioId,
                                    EditarActividadRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Actividad actividad = buscarActiva(actividadId, procesoId);

        String nombre = request.getNombre().trim();
        if (actividadRepository.existsByProcesoIdAndNombreIgnoreCaseAndActivoTrueAndIdNot(
                procesoId, nombre, actividadId)) {
            throw new RecursoDuplicadoException("Ya existe una actividad con ese nombre en el proceso");
        }

        actividad.renombrar(nombre);
        actividad.cambiarTipo(request.getTipoActividad());
        actividad.mover(request.getPosicionX(), request.getPosicionY());

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                "Actividad '" + nombre + "' actualizada");
        return convertirAResponse(actividad);
    }

    // HU-10: la eliminacion es logica, la actividad pasa a inactiva
    @Transactional
    public void eliminar(Long procesoId, Long actividadId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Actividad actividad = buscarActiva(actividadId, procesoId);

        actividad.desactivar();

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ELIMINACION,
                "Actividad '" + actividad.getNombre() + "' eliminada");
    }

    @Transactional(readOnly = true)
    public List<ActividadResponse> listar(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        List<ActividadResponse> respuesta = new ArrayList<>();
        for (Actividad actividad : actividadRepository.listarActivasPorProceso(procesoId)) {
            respuesta.add(convertirAResponse(actividad));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public ActividadResponse obtener(Long procesoId, Long actividadId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        return convertirAResponse(buscarActiva(actividadId, procesoId));
    }

    private Actividad buscarActiva(Long actividadId, Long procesoId) {
        return actividadRepository.findByIdAndProcesoIdAndActivoTrue(actividadId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));
    }

    // Si no llega poolId se usa el pool de la empresa propietaria del proceso
    private Pool buscarPoolDelProceso(Proceso proceso, Long poolId) {
        Pool encontrado = null;
        for (Pool pool : proceso.getPools()) {
            if (poolId == null && pool.esPropietario() && pool.isActivo()) {
                encontrado = pool;
            }
            if (poolId != null && poolId.equals(pool.getId()) && pool.isActivo()) {
                encontrado = pool;
            }
        }
        if (encontrado == null) {
            throw new RecursoNoEncontradoException("Pool no encontrado en el proceso");
        }
        if (encontrado.isCajaNegra()) {
            throw new SolicitudInvalidaException(
                    "Un pool de caja negra no puede tener actividades internas");
        }
        return encontrado;
    }

    private ActividadResponse convertirAResponse(Actividad actividad) {
        return modelMapper.map(actividad, ActividadResponse.class);
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar actividades");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar actividades");
        }
    }
}
