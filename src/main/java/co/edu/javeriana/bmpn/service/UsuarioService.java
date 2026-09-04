package co.edu.javeriana.bmpn.service;

import java.util.List;
import java.util.Locale;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.usuario.CambiarRolUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.RegistrarUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;
import co.edu.javeriana.bmpn.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public UsuarioService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            ModelMapper modelMapper) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
    }

    public List<UsuarioResponse> listarActivos(Long empresaId) {
        return usuarioRepository
                .findAllByEmpresaIdAndActivoTrueOrderByNombreAscApellidoAsc(empresaId)
                .stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioResponse.class))
                .toList();
    }

    @Transactional
    public UsuarioResponse registrar(
            Long empresaId,
            RolAcceso rolUsuarioAutenticado,
            RegistrarUsuarioRequest request) {
        exigirAdministrador(rolUsuarioAutenticado);

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con ese correo");
        }

        empresaRepository.findByIdAndActivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
        throw new UnsupportedOperationException(
                "El registro se completara cuando se implemente el manejo seguro de contrasenas");
    }

    @Transactional
    public UsuarioResponse cambiarRol(
            Long empresaId,
            RolAcceso rolUsuarioAutenticado,
            Long usuarioId,
            CambiarRolUsuarioRequest request) {
        exigirAdministrador(rolUsuarioAutenticado);
        Usuario usuario = buscarUsuarioActivo(empresaId, usuarioId);
        usuario.cambiarRol(request.getRolAcceso());
        return modelMapper.map(usuario, UsuarioResponse.class);
    }

    @Transactional
    public void desactivar(
            Long empresaId,
            RolAcceso rolUsuarioAutenticado,
            Long usuarioId) {
        exigirAdministrador(rolUsuarioAutenticado);
        Usuario usuario = buscarUsuarioActivo(empresaId, usuarioId);
        usuario.desactivar();
    }

    private Usuario buscarUsuarioActivo(Long empresaId, Long usuarioId) {
        return usuarioRepository.findByIdAndEmpresaIdAndActivoTrue(usuarioId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private void exigirAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede gestionar usuarios");
        }
    }
}
