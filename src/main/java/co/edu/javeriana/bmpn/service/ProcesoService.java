package co.edu.javeriana.bmpn.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.proceso.CrearProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.EditarProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.HistorialResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResumen;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.EstadoProceso;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.ProcesoRepository;

@Service
public class ProcesoService {

    private static final int ORDEN_POOL_PROPIETARIO = 0;

    private final ProcesoRepository procesoRepository;
    private final UsuarioService usuarioService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public ProcesoService(ProcesoRepository procesoRepository,
                          UsuarioService usuarioService,
                          HistorialProcesoService historialProcesoService,
                          ModelMapper modelMapper) {
        this.procesoRepository = procesoRepository;
        this.usuarioService = usuarioService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public ProcesoResponse crear(Long usuarioId, CrearProcesoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());

        Empresa empresa = usuario.getEmpresa();
        String nombre = request.getNombre().trim();
        if (procesoRepository.existsByEmpresaIdAndNombreIgnoreCase(empresa.getId(), nombre)) {
            throw new RecursoDuplicadoException("Ya existe un proceso con ese nombre en la empresa");
        }

        Proceso proceso = new Proceso(empresa, nombre,
                request.getDescripcion().trim(), request.getCategoria().trim());
        proceso.agregarPool(new Pool(empresa, empresa.getNombre(),
                TipoParticipante.EMPRESA_PROPIETARIA, ORDEN_POOL_PROPIETARIO));
        procesoRepository.save(proceso);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                "Proceso creado en estado " + proceso.getEstado());
        return convertirAResponse(proceso);
    }

    @Transactional
    public ProcesoResponse editar(Long procesoId, Long usuarioId, EditarProcesoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());

        Long empresaId = usuario.getEmpresa().getId();
        Proceso proceso = buscarActivoDeEmpresa(procesoId, empresaId);

        String nombre = request.getNombre().trim();
        if (procesoRepository.existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(empresaId, nombre, procesoId)) {
            throw new RecursoDuplicadoException("Ya existe un proceso con ese nombre en la empresa");
        }

        proceso.actualizarDatos(nombre, request.getDescripcion().trim(),
                request.getCategoria().trim(), request.getEstado());

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                "Proceso actualizado; nuevo estado " + proceso.getEstado());
        return convertirAResponse(proceso);
    }

    @Transactional
    public void eliminar(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());

        Proceso proceso = buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        proceso.desactivar();

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ELIMINACION,
                "Proceso desactivado (eliminacion logica)");
    }

    @Transactional(readOnly = true)
    public Page<ProcesoResumen> listar(Long usuarioId, String nombre, EstadoProceso estado,
                                       String categoria, boolean incluirInactivos, Pageable pageable) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);

        String nombreBuscado = null;
        if (nombre != null && !nombre.isBlank()) {
            nombreBuscado = nombre.trim();
        }

        // Por defecto solo activos; con incluirInactivos se traen todos
        Boolean activoBuscado = Boolean.TRUE;
        if (incluirInactivos) {
            activoBuscado = null;
        }

        Page<Proceso> pagina = procesoRepository.buscar(usuario.getEmpresa().getId(),
                activoBuscado, nombreBuscado, estado, categoria, pageable);
        return pagina.map(proceso -> modelMapper.map(proceso, ProcesoResumen.class));
    }

    @Transactional(readOnly = true)
    public ProcesoResponse obtenerDetalle(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        Proceso proceso = buscarDeEmpresa(procesoId, usuario.getEmpresa().getId());
        return convertirAResponse(proceso);
    }

    @Transactional(readOnly = true)
    public List<HistorialResponse> obtenerHistorial(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        // Confirma que el proceso es de la empresa del usuario antes de mostrar su historial
        buscarDeEmpresa(procesoId, usuario.getEmpresa().getId());
        return historialProcesoService.listarPorProceso(procesoId);
    }

    private Proceso buscarActivoDeEmpresa(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaIdAndActivoTrue(procesoId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proceso no encontrado"));
    }

    private Proceso buscarDeEmpresa(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proceso no encontrado"));
    }

    private ProcesoResponse convertirAResponse(Proceso proceso) {
        ProcesoResponse respuesta = modelMapper.map(proceso, ProcesoResponse.class);
        for (Pool pool : proceso.getPools()) {
            if (pool.esPropietario()) {
                respuesta.setPoolPropietarioId(pool.getId());
                respuesta.setPoolPropietarioNombre(pool.getNombre());
            }
        }
        return respuesta;
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar procesos");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar procesos");
        }
    }
}