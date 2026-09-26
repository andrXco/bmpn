package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.empresa.EmpresaResponse;
import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;
import co.edu.javeriana.bmpn.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmpresaService empresaService;

    @BeforeEach
    void prepararDatos() {
        empresaService = new EmpresaService(empresaRepository, usuarioRepository,
                passwordEncoder, new ModelMapperConfig().modelMapper());
    }

    private RegistrarEmpresaRequest solicitud() {
        return new RegistrarEmpresaRequest(" 900-abc ", " Empresa Demo ", "Contacto@Demo.co",
                "Ana", "Paz", " ADMIN@Demo.co ", "clave12345");
    }

    @Test
    void registrarEmpresa() {
        when(passwordEncoder.encode("clave12345")).thenReturn("hash-cifrado");

        EmpresaResponse respuesta = empresaService.registrar(solicitud());

        assertThat(respuesta.getNit()).isEqualTo("900-ABC");
        assertThat(respuesta.getNombre()).isEqualTo("Empresa Demo");
        assertThat(respuesta.getCorreoContacto()).isEqualTo("contacto@demo.co");
        assertThat(respuesta.getAdministradorInicial().getEmail()).isEqualTo("admin@demo.co");
        assertThat(respuesta.getAdministradorInicial().getRolAcceso()).isEqualTo(RolAcceso.ADMINISTRADOR);
        verify(empresaRepository).save(any(Empresa.class));

        ArgumentCaptor<Usuario> administrador = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(administrador.capture());
        assertThat(administrador.getValue().getPasswordHash()).isEqualTo("hash-cifrado");
    }

    @Test
    void registrarNitRepetido() {
        when(empresaRepository.existsByNit("900-ABC")).thenReturn(true);

        assertThatThrownBy(() -> empresaService.registrar(solicitud()))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(empresaRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarCorreoAdministradorRepetido() {
        when(usuarioRepository.existsByEmailIgnoreCase("admin@demo.co")).thenReturn(true);

        assertThatThrownBy(() -> empresaService.registrar(solicitud()))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(empresaRepository, never()).save(any());
    }
}
