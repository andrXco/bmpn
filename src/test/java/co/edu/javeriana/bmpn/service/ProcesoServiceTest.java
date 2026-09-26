package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.proceso.CrearProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.EditarProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResumen;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.EstadoProceso;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.ProcesoRepository;

@ExtendWith(MockitoExtension.class)
class ProcesoServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long USUARIO_ID = 5L;

    @Mock
    private ProcesoRepository procesoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    private ProcesoService procesoService;

    private Empresa empresa;

    @BeforeEach
    void prepararDatos() {
        procesoService = new ProcesoService(procesoRepository, usuarioService,
                historialProcesoService, new ModelMapperConfig().modelMapper());
        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
    }

    private Usuario usuarioConRol(RolAcceso rol) {
        Usuario usuario = new Usuario(empresa, "usuario@demo.co", "Ana", "Paz", "hash", rol);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
        return usuario;
    }

    private Proceso procesoExistente() {
        return new Proceso(empresa, "Solicitud de vacaciones", "Proceso de ejemplo", "RRHH");
    }

    @Test
    void crearProceso() {
        Usuario editor = usuarioConRol(RolAcceso.EDITOR);

        ProcesoResponse respuesta = procesoService.crear(USUARIO_ID,
                new CrearProcesoRequest("  Solicitud de vacaciones  ", "Proceso de ejemplo", "RRHH"));

        assertThat(respuesta.getNombre()).isEqualTo("Solicitud de vacaciones");
        assertThat(respuesta.getEstado()).isEqualTo(EstadoProceso.BORRADOR);
        assertThat(respuesta.isActivo()).isTrue();
        assertThat(respuesta.getPoolPropietarioNombre()).isEqualTo("Empresa Demo");
        verify(procesoRepository).save(any(Proceso.class));
        verify(historialProcesoService).registrar(any(Proceso.class), eq(editor),
                eq(AccionHistorial.CREACION), anyString());
    }

    @Test
    void crearSinPermiso() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);

        assertThatThrownBy(() -> procesoService.crear(USUARIO_ID,
                new CrearProcesoRequest("Solicitud de vacaciones", "Proceso de ejemplo", "RRHH")))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(procesoRepository, never()).save(any());
    }

    @Test
    void crearNombreRepetido() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);
        when(procesoRepository.existsByEmpresaIdAndNombreIgnoreCase(any(), eq("Solicitud de vacaciones")))
                .thenReturn(true);

        assertThatThrownBy(() -> procesoService.crear(USUARIO_ID,
                new CrearProcesoRequest("Solicitud de vacaciones", "Proceso de ejemplo", "RRHH")))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(procesoRepository, never()).save(any());
    }

    @Test
    void editarProceso() {
        Usuario editor = usuarioConRol(RolAcceso.EDITOR);
        Proceso proceso = procesoExistente();
        when(procesoRepository.findByIdAndEmpresaIdAndActivoTrue(eq(PROCESO_ID), any()))
                .thenReturn(Optional.of(proceso));

        ProcesoResponse respuesta = procesoService.editar(PROCESO_ID, USUARIO_ID,
                new EditarProcesoRequest("Vacaciones 2026", "Nueva descripcion", "Talento humano",
                        EstadoProceso.PUBLICADO));

        assertThat(respuesta.getNombre()).isEqualTo("Vacaciones 2026");
        assertThat(respuesta.getCategoria()).isEqualTo("Talento humano");
        assertThat(respuesta.getEstado()).isEqualTo(EstadoProceso.PUBLICADO);
        verify(historialProcesoService).registrar(eq(proceso), eq(editor),
                eq(AccionHistorial.ACTUALIZACION), anyString());
    }

    @Test
    void editarNombreRepetido() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoRepository.findByIdAndEmpresaIdAndActivoTrue(eq(PROCESO_ID), any()))
                .thenReturn(Optional.of(procesoExistente()));
        when(procesoRepository.existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(any(), eq("Compras"), eq(PROCESO_ID)))
                .thenReturn(true);

        assertThatThrownBy(() -> procesoService.editar(PROCESO_ID, USUARIO_ID,
                new EditarProcesoRequest("Compras", "x", "y", EstadoProceso.BORRADOR)))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void editarProcesoInexistente() {
        usuarioConRol(RolAcceso.EDITOR);
        when(procesoRepository.findByIdAndEmpresaIdAndActivoTrue(eq(PROCESO_ID), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> procesoService.editar(PROCESO_ID, USUARIO_ID,
                new EditarProcesoRequest("Compras", "x", "y", EstadoProceso.BORRADOR)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarProceso() {
        Usuario administrador = usuarioConRol(RolAcceso.ADMINISTRADOR);
        Proceso proceso = procesoExistente();
        when(procesoRepository.findByIdAndEmpresaIdAndActivoTrue(eq(PROCESO_ID), any()))
                .thenReturn(Optional.of(proceso));

        procesoService.eliminar(PROCESO_ID, USUARIO_ID);

        assertThat(proceso.isActivo()).isFalse();
        verify(procesoRepository, never()).delete(any());
        verify(historialProcesoService).registrar(eq(proceso), eq(administrador),
                eq(AccionHistorial.ELIMINACION), anyString());
    }

    @Test
    void eliminarSinPermiso() {
        usuarioConRol(RolAcceso.EDITOR);

        assertThatThrownBy(() -> procesoService.eliminar(PROCESO_ID, USUARIO_ID))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void listarSoloActivos() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        PageRequest pagina = PageRequest.of(0, 20);
        when(procesoRepository.buscar(any(), eq(Boolean.TRUE), eq("vac"), isNull(), isNull(), eq(pagina)))
                .thenReturn(new PageImpl<>(List.of(procesoExistente())));

        Page<ProcesoResumen> resultado = procesoService.listar(USUARIO_ID, "  vac ", null, null, false, pagina);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNombre()).isEqualTo("Solicitud de vacaciones");
    }

    @Test
    void listarConInactivos() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        PageRequest pagina = PageRequest.of(0, 20);
        when(procesoRepository.buscar(any(), isNull(), isNull(), isNull(), isNull(), eq(pagina)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ProcesoResumen> resultado = procesoService.listar(USUARIO_ID, "", null, null, true, pagina);

        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    void obtenerDetalleInexistente() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        when(procesoRepository.findByIdAndEmpresaId(eq(PROCESO_ID), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> procesoService.obtenerDetalle(PROCESO_ID, USUARIO_ID))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerHistorial() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        when(procesoRepository.findByIdAndEmpresaId(eq(PROCESO_ID), any()))
                .thenReturn(Optional.of(procesoExistente()));

        procesoService.obtenerHistorial(PROCESO_ID, USUARIO_ID);

        verify(historialProcesoService).listarPorProceso(PROCESO_ID);
    }
}
