package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.usuario.CambiarRolUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.RegistrarUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;
import co.edu.javeriana.bmpn.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final Long SOLICITANTE_ID = 1L;
    private static final Long OBJETIVO_ID = 2L;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    private Empresa empresa;

    @BeforeEach
    void prepararDatos() {
        usuarioService = new UsuarioService(empresaRepository, usuarioRepository,
                passwordEncoder, new ModelMapperConfig().modelMapper());
        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
    }

    private Usuario solicitanteConRol(RolAcceso rol) {
        Usuario solicitante = new Usuario(empresa, "admin@demo.co", "Ana", "Paz", "hash", rol);
        when(usuarioRepository.findByIdAndActivoTrue(SOLICITANTE_ID)).thenReturn(Optional.of(solicitante));
        return solicitante;
    }

    private Usuario usuarioObjetivo() {
        Usuario objetivo = new Usuario(empresa, "luis@demo.co", "Luis", "Rojas", "hash", RolAcceso.EDITOR);
        when(usuarioRepository.findByIdAndEmpresaIdAndActivoTrue(eq(OBJETIVO_ID), any()))
                .thenReturn(Optional.of(objetivo));
        return objetivo;
    }

    @Test
    void listarUsuarios() {
        solicitanteConRol(RolAcceso.SOLO_LECTURA);
        Usuario otro = new Usuario(empresa, "luis@demo.co", "Luis", "Rojas", "hash", RolAcceso.EDITOR);
        when(usuarioRepository.findAllByEmpresaIdAndActivoTrueOrderByNombreAscApellidoAsc(any()))
                .thenReturn(List.of(otro));

        List<UsuarioResponse> usuarios = usuarioService.listarActivos(SOLICITANTE_ID);

        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("luis@demo.co");
    }

    @Test
    void listarConUsuarioInexistente() {
        when(usuarioRepository.findByIdAndActivoTrue(SOLICITANTE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.listarActivos(SOLICITANTE_ID))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void registrarUsuario() {
        solicitanteConRol(RolAcceso.ADMINISTRADOR);
        when(empresaRepository.findByIdAndActivoTrue(any())).thenReturn(Optional.of(empresa));
        when(passwordEncoder.encode("clave12345")).thenReturn("hash-cifrado");

        UsuarioResponse respuesta = usuarioService.registrar(SOLICITANTE_ID,
                new RegistrarUsuarioRequest(" NUEVO@Demo.co ", "Luis", "Rojas", "clave12345", RolAcceso.EDITOR));

        assertThat(respuesta.getEmail()).isEqualTo("nuevo@demo.co");
        assertThat(respuesta.getRolAcceso()).isEqualTo(RolAcceso.EDITOR);

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getPasswordHash()).isEqualTo("hash-cifrado");
        assertThat(guardado.getValue().getEmpresa()).isEqualTo(empresa);
    }

    @Test
    void registrarSinPermiso() {
        solicitanteConRol(RolAcceso.EDITOR);

        RegistrarUsuarioRequest solicitud =
                new RegistrarUsuarioRequest("nuevo@demo.co", "Luis", "Rojas", "clave12345", RolAcceso.EDITOR);
        assertThatThrownBy(() -> usuarioService.registrar(SOLICITANTE_ID, solicitud))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarCorreoRepetido() {
        solicitanteConRol(RolAcceso.ADMINISTRADOR);
        when(usuarioRepository.existsByEmailIgnoreCase("nuevo@demo.co")).thenReturn(true);

        RegistrarUsuarioRequest solicitud =
                new RegistrarUsuarioRequest("nuevo@demo.co", "Luis", "Rojas", "clave12345", RolAcceso.EDITOR);
        assertThatThrownBy(() -> usuarioService.registrar(SOLICITANTE_ID, solicitud))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarRol() {
        solicitanteConRol(RolAcceso.ADMINISTRADOR);
        Usuario objetivo = usuarioObjetivo();

        UsuarioResponse respuesta = usuarioService.cambiarRol(SOLICITANTE_ID, OBJETIVO_ID,
                new CambiarRolUsuarioRequest(RolAcceso.SOLO_LECTURA));

        assertThat(objetivo.getRolAcceso()).isEqualTo(RolAcceso.SOLO_LECTURA);
        assertThat(respuesta.getRolAcceso()).isEqualTo(RolAcceso.SOLO_LECTURA);
    }

    @Test
    void cambiarRolUsuarioDeOtraEmpresa() {
        solicitanteConRol(RolAcceso.ADMINISTRADOR);
        when(usuarioRepository.findByIdAndEmpresaIdAndActivoTrue(eq(OBJETIVO_ID), any()))
                .thenReturn(Optional.empty());

        CambiarRolUsuarioRequest solicitud = new CambiarRolUsuarioRequest(RolAcceso.EDITOR);
        assertThatThrownBy(() -> usuarioService.cambiarRol(SOLICITANTE_ID, OBJETIVO_ID, solicitud))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void desactivarUsuario() {
        solicitanteConRol(RolAcceso.ADMINISTRADOR);
        Usuario objetivo = usuarioObjetivo();

        usuarioService.desactivar(SOLICITANTE_ID, OBJETIVO_ID);

        assertThat(objetivo.isActivo()).isFalse();
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void desactivarSinPermiso() {
        solicitanteConRol(RolAcceso.SOLO_LECTURA);

        assertThatThrownBy(() -> usuarioService.desactivar(SOLICITANTE_ID, OBJETIVO_ID))
                .isInstanceOf(AccesoDenegadoException.class);
    }
}
