package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.lane.CrearLaneRequest;
import co.edu.javeriana.bmpn.dto.lane.EditarLaneRequest;
import co.edu.javeriana.bmpn.dto.lane.LaneResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Lane;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.RolProceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.LaneRepository;

//La lane distingue quien responde por cada actividad
//22
@Service
public class LaneService {

    private final LaneRepository laneRepository;
    private final UsuarioService usuarioService;
    private final ProcesoService procesoService;
    private final PoolService poolService;
    private final RolProcesoService rolProcesoService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public LaneService(LaneRepository laneRepository,
                       UsuarioService usuarioService,
                       ProcesoService procesoService,
                       PoolService poolService,
                       RolProcesoService rolProcesoService,
                       HistorialProcesoService historialProcesoService,
                       ModelMapper modelMapper) {
        this.laneRepository = laneRepository;
        this.usuarioService = usuarioService;
        this.procesoService = procesoService;
        this.poolService = poolService;
        this.rolProcesoService = rolProcesoService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public LaneResponse crear(Long procesoId, Long poolId, Long usuarioId, CrearLaneRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Pool pool = poolService.buscarActivo(poolId, procesoId);

        RolProceso rol = resolverRolDeLaEmpresaDelPool(pool, request.getRolProcesoId());
        if (laneRepository.existsByPoolIdAndRolProcesoId(poolId, rol.getId())) {
            throw new RecursoDuplicadoException("Ya existe una lane con ese rol en el pool");
        }

        int orden = (int) laneRepository.countByPoolId(poolId);
        Lane lane = new Lane(pool, rol, orden);
        laneRepository.save(lane);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                "Lane de rol '" + rol.getNombre() + "' agregada al pool '" + pool.getNombre() + "'");
        return convertirAResponse(lane);
    }

    @Transactional
    public LaneResponse editar(Long procesoId, Long poolId, Long laneId, Long usuarioId,
                               EditarLaneRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Pool pool = poolService.buscarActivo(poolId, procesoId);
        Lane lane = buscarActiva(laneId, poolId);

        RolProceso rol = resolverRolDeLaEmpresaDelPool(pool, request.getRolProcesoId());
        if (laneRepository.existsByPoolIdAndRolProcesoIdAndIdNot(poolId, rol.getId(), laneId)) {
            throw new RecursoDuplicadoException("Ya existe una lane con ese rol en el pool");
        }
        lane.cambiarRol(rol);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                "Lane del pool '" + pool.getNombre() + "' actualizada");
        return convertirAResponse(lane);
    }

    @Transactional
    public void eliminar(Long procesoId, Long poolId, Long laneId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Pool pool = poolService.buscarActivo(poolId, procesoId);
        Lane lane = buscarActiva(laneId, poolId);

        if (lane.tieneElementosActivos()) {
            throw new SolicitudInvalidaException(
                    "No se puede eliminar una lane que contiene actividades; primero reasignelas a otra lane");
        }
        lane.desactivar();

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ELIMINACION,
                "Lane eliminada del pool '" + pool.getNombre() + "'");
    }

    @Transactional(readOnly = true)
    public List<LaneResponse> listar(Long procesoId, Long poolId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        poolService.buscarActivo(poolId, procesoId);

        List<LaneResponse> respuesta = new ArrayList<>();
        for (Lane lane : laneRepository.findAllByPoolIdAndActivoTrueOrderByOrdenAsc(poolId)) {
            respuesta.add(convertirAResponse(lane));
        }
        return respuesta;
    }

    // Para ActividadService: la lane debe estar en el mismo pool que la actividad
    public Lane buscarActiva(Long laneId, Long poolId) {
        return laneRepository.findByIdAndPoolIdAndActivoTrue(laneId, poolId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lane no encontrada en el pool"));
    }

    private RolProceso resolverRolDeLaEmpresaDelPool(Pool pool, Long rolProcesoId) {
        if (pool.getEmpresaParticipante() == null) {
            throw new SolicitudInvalidaException("Un pool de sistema externo no puede tener lanes internas");
        }
        return rolProcesoService.buscarActivo(rolProcesoId, pool.getEmpresaParticipante().getId());
    }

    private LaneResponse convertirAResponse(Lane lane) {
        return modelMapper.map(lane, LaneResponse.class);
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar lanes");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar lanes");
        }
    }
}