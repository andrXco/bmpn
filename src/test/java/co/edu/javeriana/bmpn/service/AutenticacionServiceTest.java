package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;
import co.edu.javeriana.bmpn.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AutenticacionService autenticacionService;

    private Usuario usuario;

    @BeforeEach
    void prepararDatos() {
        autenticacionService = new AutenticacionService(usuarioRepository, passwordEncoder);
        Empresa empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash-cifrado", RolAcceso.EDITOR);
    }

    @Test
    void iniciarSesion() {
        when(usuarioRepository.findByEmailIgnoreCaseAndActivoTrue("ana@demo.co")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave12345", "hash-cifrado")).thenReturn(true);

        SesionUsuarioResponse sesion = autenticacionService.iniciarSesion(
                new IniciarSesionRequest(" ANA@Demo.co ", "clave12345"));

        assertThat(sesion.getEmail()).isEqualTo("ana@demo.co");
        assertThat(sesion.getNombreCompleto()).isEqualTo("Ana Paz");
        assertThat(sesion.getRolAcceso()).isEqualTo(RolAcceso.EDITOR);
    }

    @Test
    void iniciarSesionCorreoInexistente() {
        when(usuarioRepository.findByEmailIgnoreCaseAndActivoTrue("otro@demo.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticacionService.iniciarSesion(
                new IniciarSesionRequest("otro@demo.co", "clave12345")))
                .isInstanceOf(AutenticacionRequeridaException.class)
                .hasMessage("Correo o contrasena invalidos");
    }

    @Test
    void iniciarSesionContrasenaIncorrecta() {
        when(usuarioRepository.findByEmailIgnoreCaseAndActivoTrue("ana@demo.co")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("equivocada", "hash-cifrado")).thenReturn(false);

        // Mismo mensaje que con un correo inexistente: no revela si el correo esta registrado
        assertThatThrownBy(() -> autenticacionService.iniciarSesion(
                new IniciarSesionRequest("ana@demo.co", "equivocada")))
                .isInstanceOf(AutenticacionRequeridaException.class)
                .hasMessage("Correo o contrasena invalidos");
    }
}
