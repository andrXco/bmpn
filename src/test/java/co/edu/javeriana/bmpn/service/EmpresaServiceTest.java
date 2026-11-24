package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.javeriana.bmpn.dto.empresa.EmpresaResponse;
import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioService usuarioService;

    private EmpresaService empresaService;

    @BeforeEach
    void prepararDatos() {
        empresaService = new EmpresaService(empresaRepository, usuarioService);
    }

    private RegistrarEmpresaRequest solicitud() {
        return new RegistrarEmpresaRequest(" 900-abc ", " Empresa Demo ", "Contacto@Demo.co",
                "Ana", "Paz", " ADMIN@Demo.co ", "clave12345");
    }

    @Test
    void registrarEmpresa() {
        UsuarioResponse administrador = new UsuarioResponse(1L, null, "admin@demo.co", "Ana", "Paz",
                RolAcceso.ADMINISTRADOR, null, true);
        when(usuarioService.crearAdministrador(any(Empresa.class), eq("admin@demo.co"), eq("Ana"), eq("Paz"),
                eq("clave12345"))).thenReturn(administrador);

        EmpresaResponse respuesta = empresaService.registrar(solicitud());

        assertThat(respuesta.getNit()).isEqualTo("900-ABC");
        assertThat(respuesta.getNombre()).isEqualTo("Empresa Demo");
        assertThat(respuesta.getCorreoContacto()).isEqualTo("contacto@demo.co");
        assertThat(respuesta.getAdministradorInicial()).isEqualTo(administrador);

        // El administrador queda en la empresa que se acaba de guardar
        ArgumentCaptor<Empresa> empresa = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepository).save(empresa.capture());
        verify(usuarioService).crearAdministrador(empresa.getValue(), "admin@demo.co", "Ana", "Paz", "clave12345");
    }

    @Test
    void registrarNitRepetido() {
        when(empresaRepository.existsByNit("900-ABC")).thenReturn(true);

        RegistrarEmpresaRequest solicitud = solicitud();
        assertThatThrownBy(() -> empresaService.registrar(solicitud))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(empresaRepository, never()).save(any());
        verify(usuarioService, never()).crearAdministrador(any(), any(), any(), any(), any());
    }

    @Test
    void registrarCorreoAdministradorRepetido() {
        when(usuarioService.existeEmail("admin@demo.co")).thenReturn(true);

        RegistrarEmpresaRequest solicitud = solicitud();
        assertThatThrownBy(() -> empresaService.registrar(solicitud))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void baseVaciaSinEmpresas() {
        when(empresaRepository.count()).thenReturn(0L);

        assertThat(empresaService.hayEmpresasRegistradas()).isFalse();
    }

    @Test
    void baseConEmpresas() {
        when(empresaRepository.count()).thenReturn(2L);

        assertThat(empresaService.hayEmpresasRegistradas()).isTrue();
    }
}
