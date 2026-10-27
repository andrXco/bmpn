package co.edu.javeriana.bmpn.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.usuario.CambiarRolUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.RegistrarUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public List<UsuarioResponse> listarActivos(Long usuarioSolicitanteId) {
        Usuario solicitante = buscarActivo(usuarioSolicitanteId);

        return usuarioRepository
                .findAllByEmpresaIdAndActivoTrueOrderByNombreAscApellidoAsc(
                        solicitante.getEmpresa().getId())
                .stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioResponse.class))
                .toList();
    }

    @Transactional
    public UsuarioResponse registrar(
            Long usuarioSolicitanteId,
            RegistrarUsuarioRequest request) {
        Usuario solicitante = buscarActivo(usuarioSolicitanteId);
        exigirAdministrador(solicitante.getRolAcceso());

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con ese correo");
        }

        // El usuario nuevo queda en la empresa de quien lo registra
        Empresa empresa = solicitante.getEmpresa();
        if (!empresa.isActivo()) {
            throw new RecursoNoEncontradoException("Empresa no encontrada");
        }

        // Igual que en crearAdministrador: el texto plano solo se lee para
        // producir el hash, en la misma linea en que se construye la entidad.
        Usuario usuario = new Usuario(
                empresa,
                email,
                request.getNombre().trim(),
                request.getApellido().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getRolAcceso());
        usuarioRepository.save(usuario);

        return modelMapper.map(usuario, UsuarioResponse.class);
    }
    @Transactional
    public UsuarioResponse cambiarRol(
            Long usuarioSolicitanteId,
            Long usuarioObjetivoId,
            CambiarRolUsuarioRequest request) {

        Usuario solicitante = buscarActivo(usuarioSolicitanteId);
        exigirAdministrador(solicitante.getRolAcceso());

        Usuario usuarioObjetivo = buscarUsuarioActivo(
                solicitante.getEmpresa().getId(),
                usuarioObjetivoId);

        usuarioObjetivo.cambiarRol(request.getRolAcceso());

        return modelMapper.map(usuarioObjetivo, UsuarioResponse.class);
    }

    @Transactional
    public void desactivar(
            Long usuarioSolicitanteId,
            Long usuarioObjetivoId) {
        Usuario solicitante = buscarActivo(usuarioSolicitanteId);
        exigirAdministrador(solicitante.getRolAcceso());

        Usuario usuario = buscarUsuarioActivo(
                solicitante.getEmpresa().getId(),
                usuarioObjetivoId);
        usuario.desactivar();
    }

    public Usuario buscarActivo(Long usuarioId) {
        return usuarioRepository.findByIdAndActivoTrue(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    // Para AutenticacionService: si no existe, el inicio de sesion responde con su propio mensaje
    public Optional<Usuario> buscarActivoPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(email);
    }

    // Para EmpresaService: el correo se revisa antes de crear la empresa
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(email);
    }

    // Para EmpresaService: el primer usuario de una empresa nueva es su administrador.
    // La contrasena en texto plano solo se lee para producir el hash.
    @Transactional
    public UsuarioResponse crearAdministrador(
            Empresa empresa,
            String email,
            String nombre,
            String apellido,
            String password) {
        Usuario administrador = new Usuario(
                empresa,
                email,
                nombre,
                apellido,
                passwordEncoder.encode(password),
                RolAcceso.ADMINISTRADOR);
        usuarioRepository.save(administrador);

        return modelMapper.map(administrador, UsuarioResponse.class);
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
