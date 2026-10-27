package co.edu.javeriana.bmpn.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;

@Service
public class AutenticacionService {

    // Mensaje deliberadamente generico: no distingue entre "correo no
    // existe" y "contrasena incorrecta" para no revelar a un atacante
    // que correos estan registrados en el sistema.
    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Correo o contrasena invalidos";

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public AutenticacionService(
            UsuarioService usuarioService,
            PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public SesionUsuarioResponse iniciarSesion(IniciarSesionRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioService.buscarActivoPorEmail(email)
                .orElseThrow(() -> new AutenticacionRequeridaException(MENSAJE_CREDENCIALES_INVALIDAS));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new AutenticacionRequeridaException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        return new SesionUsuarioResponse(
                usuario.getId(),
                usuario.getEmpresa().getId(),
                usuario.getEmail(),
                usuario.getNombre() + " " + usuario.getApellido(),
                usuario.getRolAcceso());
    }
}
