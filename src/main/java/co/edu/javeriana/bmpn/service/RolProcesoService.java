package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.rolproceso.CrearRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.EditarRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.RolProcesoResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.RolProceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.RolProcesoRepository;

@Service
public class RolProcesoService {

    private final RolProcesoRepository rolProcesoRepository;
    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public RolProcesoService(RolProcesoRepository rolProcesoRepository,
                             UsuarioService usuarioService,
                             ModelMapper modelMapper) {
        this.rolProcesoRepository = rolProcesoRepository;
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }

    //17
    @Transactional
    public RolProcesoResponse crear(Long usuarioId, CrearRolProcesoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());

        Empresa empresa = usuario.getEmpresa();
        String nombre = request.getNombre().trim();
        if (rolProcesoRepository.existsByEmpresaIdAndNombreIgnoreCase(empresa.getId(), nombre)) {
            throw new RecursoDuplicadoException("Ya existe un rol de proceso con ese nombre en la empresa");
        }

        String descripcion = request.getDescripcion() == null ? null : request.getDescripcion().trim();
        RolProceso rol = new RolProceso(empresa, nombre, descripcion);
        rolProcesoRepository.save(rol);
        return convertirAResponse(rol);
    }

    //18
    @Transactional
    public RolProcesoResponse editar(Long rolProcesoId, Long usuarioId, EditarRolProcesoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());

        Long empresaId = usuario.getEmpresa().getId();
        RolProceso rol = buscarActivo(rolProcesoId, empresaId);

        String nombre = request.getNombre().trim();
        if (rolProcesoRepository.existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(empresaId, nombre, rolProcesoId)) {
            throw new RecursoDuplicadoException("Ya existe un rol de proceso con ese nombre en la empresa");
        }

        String descripcion = request.getDescripcion() == null ? null : request.getDescripcion().trim();
        rol.actualizarDatos(nombre, descripcion);
        return convertirAResponse(rol);
    }

    //19 eliminacion logica
    @Transactional
    public void eliminar(Long rolProcesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());

        RolProceso rol = buscarActivo(rolProcesoId, usuario.getEmpresa().getId());
        rol.desactivar();
    }

    //20
    @Transactional(readOnly = true)
    public List<RolProcesoResponse> listar(Long usuarioId, boolean incluirInactivos) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        Long empresaId = usuario.getEmpresa().getId();

        List<RolProceso> roles = incluirInactivos
                ? rolProcesoRepository.findAllByEmpresaIdOrderByNombreAsc(empresaId)
                : rolProcesoRepository.findAllByEmpresaIdAndActivoTrueOrderByNombreAsc(empresaId);

        List<RolProcesoResponse> respuesta = new ArrayList<>();
        for (RolProceso rol : roles) {
            respuesta.add(convertirAResponse(rol));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public RolProcesoResponse obtener(Long rolProcesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        return convertirAResponse(buscarActivo(rolProcesoId, usuario.getEmpresa().getId()));
    }

    public RolProceso buscarActivo(Long rolProcesoId, Long empresaId) {
        return rolProcesoRepository.findByIdAndEmpresaIdAndActivoTrue(rolProcesoId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol de proceso no encontrado"));
    }

    private RolProcesoResponse convertirAResponse(RolProceso rol) {
        return modelMapper.map(rol, RolProcesoResponse.class);
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar roles de proceso");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar roles de proceso");
        }
    }
}