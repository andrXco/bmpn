package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.pool.AsociarRolPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.CrearPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.EditarPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.PermisoPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.PermisoPoolResponse;
import co.edu.javeriana.bmpn.dto.pool.PoolResponse;
import co.edu.javeriana.bmpn.dto.pool.RolPoolResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.PermisoPool;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.PoolRolDisponible;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.RolProceso;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.PoolRepository;

@Service
public class PoolService {

    private final PoolRepository poolRepository;
    private final PoolRolDisponibleService poolRolDisponibleService;
    private final PermisoPoolService permisoPoolService;
    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;
    private final ProcesoService procesoService;
    private final ProcesoCompartidoService procesoCompartidoService;
    private final RolProcesoService rolProcesoService;
    private final HistorialProcesoService historialProcesoService;
    private final ArcoService arcoService;
    private final ModelMapper modelMapper;

    public PoolService(PoolRepository poolRepository,
                       PoolRolDisponibleService poolRolDisponibleService,
                       PermisoPoolService permisoPoolService,
                       EmpresaService empresaService,
                       UsuarioService usuarioService,
                       ProcesoService procesoService,
                       ProcesoCompartidoService procesoCompartidoService,
                       RolProcesoService rolProcesoService,
                       HistorialProcesoService historialProcesoService,
                       ArcoService arcoService,
                       ModelMapper modelMapper) {
        this.poolRepository = poolRepository;
        this.poolRolDisponibleService = poolRolDisponibleService;
        this.permisoPoolService = permisoPoolService;
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.procesoService = procesoService;
        this.procesoCompartidoService = procesoCompartidoService;
        this.rolProcesoService = rolProcesoService;
        this.historialProcesoService = historialProcesoService;
        this.arcoService = arcoService;
        this.modelMapper = modelMapper;
    }

    //21
    @Transactional
    public PoolResponse crear(Long procesoId, Long usuarioId, CrearPoolRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        String nombre = request.getNombre().trim();
        if (poolRepository.existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(procesoId, nombre)) {
            throw new RecursoDuplicadoException("Ya existe un pool con ese nombre en el proceso");
        }

        TipoParticipante tipo = request.getTipoParticipante();
        if (tipo == TipoParticipante.EMPRESA_PROPIETARIA) {
            throw new SolicitudInvalidaException(
                    "El pool de la empresa propietaria se crea automaticamente con el proceso");
        }

        Empresa empresaParticipante = resolverEmpresaParticipante(proceso, tipo, request.getEmpresaParticipanteId());

        int orden = (int) poolRepository.countByProcesoId(procesoId);
        Pool pool = new Pool(empresaParticipante, nombre, tipo, orden,
                request.getCanalExterno(), request.isCajaNegra());
        pool.asignarProceso(proceso);
        poolRepository.save(pool);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                describir(nombre) + " agregado al proceso");
        return convertirAResponse(pool);
    }

    //22 (edicion basica de pool, no cambia tipo ni empresa participante)
    @Transactional
    public PoolResponse editar(Long procesoId, Long poolId, Long usuarioId, EditarPoolRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Pool pool = buscarActivo(poolId, procesoId);

        String nombre = request.getNombre().trim();
        if (poolRepository.existsByProcesoIdAndNombreIgnoreCaseAndActivoTrueAndIdNot(procesoId, nombre, poolId)) {
            throw new RecursoDuplicadoException("Ya existe un pool con ese nombre en el proceso");
        }

        pool.renombrar(nombre);
        pool.actualizarConfiguracion(request.getCanalExterno(), request.isCajaNegra());

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                describir(nombre) + " actualizado");
        return convertirAResponse(pool);
    }

    @Transactional
    public void eliminar(Long procesoId, Long poolId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Pool pool = buscarActivo(poolId, procesoId);

        if (pool.esPropietario()) {
            throw new SolicitudInvalidaException("El pool de la empresa propietaria no se puede eliminar");
        }
        // El pool desactiva sus lanes, elementos y mensajes, los arcos se desactivan aparte
        pool.desactivar();
        arcoService.desactivarArcosDePool(poolId);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ELIMINACION,
                describir(pool.getNombre()) + " eliminado con todo su contenido");
    }

    @Transactional(readOnly = true)
    public List<PoolResponse> listar(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        List<PoolResponse> respuesta = new ArrayList<>();
        for (Pool pool : poolRepository.findAllByProcesoIdAndActivoTrueOrderByOrdenAsc(procesoId)) {
            respuesta.add(convertirAResponse(pool));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public PoolResponse obtener(Long procesoId, Long poolId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        return convertirAResponse(buscarActivo(poolId, procesoId));
    }

    //24 roles disponibles del pool
    @Transactional
    public RolPoolResponse asociarRol(Long procesoId, Long poolId, Long usuarioId, AsociarRolPoolRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Pool pool = buscarActivo(poolId, procesoId);

        if (pool.getEmpresaParticipante() == null) {
            throw new SolicitudInvalidaException(
                    "Solo se pueden asociar roles a un pool que representa una empresa participante");
        }
        RolProceso rol = rolProcesoService.buscarActivo(
                request.getRolProcesoId(), pool.getEmpresaParticipante().getId());

        PoolRolDisponible disponible = poolRolDisponibleService.habilitar(pool, rol);
        return convertirAResponse(disponible);
    }

    @Transactional
    public void desvincularRol(Long procesoId, Long poolId, Long rolProcesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        buscarActivo(poolId, procesoId);

        poolRolDisponibleService.deshabilitar(poolId, rolProcesoId);
    }

    @Transactional(readOnly = true)
    public List<RolPoolResponse> listarRolesDisponibles(Long procesoId, Long poolId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        buscarActivo(poolId, procesoId);

        List<RolPoolResponse> respuesta = new ArrayList<>();
        for (PoolRolDisponible disponible : poolRolDisponibleService.listarActivos(poolId)) {
            respuesta.add(convertirAResponse(disponible));
        }
        return respuesta;
    }

    //24 permisos por pool
    @Transactional
    public PermisoPoolResponse definirPermiso(Long procesoId, Long poolId, Long usuarioId,
                                              PermisoPoolRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Pool pool = buscarActivo(poolId, procesoId);

        PermisoPool permiso = permisoPoolService.definir(pool, request.getRolAcceso(),
                request.isPuedeCrear(), request.isPuedeEditar(), request.isPuedeEliminar());
        return convertirAResponse(permiso);
    }

    @Transactional(readOnly = true)
    public List<PermisoPoolResponse> listarPermisos(Long procesoId, Long poolId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        buscarActivo(poolId, procesoId);

        List<PermisoPoolResponse> respuesta = new ArrayList<>();
        for (PermisoPool permiso : permisoPoolService.listar(poolId)) {
            respuesta.add(convertirAResponse(permiso));
        }
        return respuesta;
    }

    public Pool buscarActivo(Long poolId, Long procesoId) {
        return poolRepository.findByIdAndProcesoIdAndActivoTrue(poolId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pool no encontrado en el proceso"));
    }

    //21 y 23: resuelve y valida el alcance de la empresa participante
    private Empresa resolverEmpresaParticipante(Proceso proceso, TipoParticipante tipo, Long empresaParticipanteId) {
        if (tipo == TipoParticipante.SISTEMA_EXTERNO) {
            return null;
        }
        if (empresaParticipanteId == null) {
            throw new SolicitudInvalidaException(
                    "Un pool de tipo CLIENTE o PROVEEDOR requiere la empresa participante");
        }
        Empresa empresa = empresaService.buscarActiva(empresaParticipanteId);

        if (empresa.getId().equals(proceso.getEmpresa().getId())) {
            throw new SolicitudInvalidaException(
                    "La empresa participante debe ser distinta de la empresa propietaria del proceso");
        }
        if (!procesoCompartidoService.estaCompartidoCon(proceso.getId(), empresa.getId())) {
            throw new SolicitudInvalidaException(
                    "La empresa participante no ha sido invitada a colaborar en este proceso");
        }
        return empresa;
    }

    private PoolResponse convertirAResponse(Pool pool) {
        return modelMapper.map(pool, PoolResponse.class);
    }

    private RolPoolResponse convertirAResponse(PoolRolDisponible disponible) {
        RolPoolResponse respuesta = new RolPoolResponse();
        respuesta.setPoolId(disponible.getPool().getId());
        respuesta.setRolProcesoId(disponible.getRolProceso().getId());
        respuesta.setNombreRol(disponible.getRolProceso().getNombre());
        respuesta.setActivo(disponible.isActivo());
        return respuesta;
    }

    private PermisoPoolResponse convertirAResponse(PermisoPool permiso) {
        PermisoPoolResponse respuesta = modelMapper.map(permiso, PermisoPoolResponse.class);
        respuesta.setPoolId(permiso.getPool().getId());
        return respuesta;
    }

    // Nombre del elemento como se muestra en el historial
    private String describir(String nombre) {
        return "Pool '" + nombre + "'";
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar pools");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede realizar esta accion sobre el pool");
        }
    }
}