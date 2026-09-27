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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.rolproceso.CrearRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.EditarRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.RolProcesoResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Lane;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.RolProceso;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.RolProcesoRepository;

@ExtendWith(MockitoExtension.class)
class RolProcesoServiceTest {

    private static final Long USUARIO_ID = 5L;
    private static final Long ROL_ID = 7L;

    @Mock
    private RolProcesoRepository rolProcesoRepository;

    @Mock
    private UsuarioService usuarioService;

    private RolProcesoService rolProcesoService;

    private Empresa empresa;
    private RolProceso rol;

    @BeforeEach
    void prepararDatos() {
        rolProcesoService = new RolProcesoService(rolProcesoRepository, usuarioService,
                new ModelMapperConfig().modelMapper());
        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        rol = new RolProceso(empresa, "Empleado", "Quien solicita");
    }

    private void usuarioConRol(RolAcceso rolAcceso) {
        Usuario usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", rolAcceso);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
    }

    private void usarRolEnUnaLane() {
        Proceso proceso = new Proceso(empresa, "Vacaciones", "Solicitud", "RRHH");
        Pool pool = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        proceso.agregarPool(pool);
        rol.getLanes().add(new Lane(pool, rol, 0));
    }

    @Test
    void crearRol() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);

        RolProcesoResponse respuesta = rolProcesoService.crear(USUARIO_ID,
                new CrearRolProcesoRequest(" Analista ", "Revisa solicitudes"));

        assertThat(respuesta.getNombre()).isEqualTo("Analista");
        assertThat(respuesta.isPuedeEliminarse()).isTrue();
        verify(rolProcesoRepository).save(any(RolProceso.class));
    }

    @Test
    void crearSinSerAdministrador() {
        usuarioConRol(RolAcceso.EDITOR);

        assertThatThrownBy(() -> rolProcesoService.crear(USUARIO_ID,
                new CrearRolProcesoRequest("Analista", null)))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(rolProcesoRepository, never()).save(any());
    }

    @Test
    void crearNombreRepetido() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);
        when(rolProcesoRepository.existsByEmpresaIdAndNombreIgnoreCase(any(), eq("Analista"))).thenReturn(true);

        assertThatThrownBy(() -> rolProcesoService.crear(USUARIO_ID,
                new CrearRolProcesoRequest("Analista", null)))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void editarRol() {
        usuarioConRol(RolAcceso.EDITOR);
        when(rolProcesoRepository.findByIdAndEmpresaIdAndActivoTrue(eq(ROL_ID), any())).thenReturn(Optional.of(rol));

        RolProcesoResponse respuesta = rolProcesoService.editar(ROL_ID, USUARIO_ID,
                new EditarRolProcesoRequest("Solicitante", "Empleado que pide vacaciones"));

        assertThat(respuesta.getNombre()).isEqualTo("Solicitante");
        assertThat(rol.getDescripcion()).isEqualTo("Empleado que pide vacaciones");
    }

    @Test
    void eliminarRolLibre() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);
        when(rolProcesoRepository.findByIdAndEmpresaIdAndActivoTrue(eq(ROL_ID), any())).thenReturn(Optional.of(rol));

        rolProcesoService.eliminar(ROL_ID, USUARIO_ID);

        assertThat(rol.isActivo()).isFalse();
    }

    @Test
    void eliminarRolEnUso() {
        usuarioConRol(RolAcceso.ADMINISTRADOR);
        usarRolEnUnaLane();
        when(rolProcesoRepository.findByIdAndEmpresaIdAndActivoTrue(eq(ROL_ID), any())).thenReturn(Optional.of(rol));

        assertThatThrownBy(() -> rolProcesoService.eliminar(ROL_ID, USUARIO_ID))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("Vacaciones");
        assertThat(rol.isActivo()).isTrue();
    }

    @Test
    void listarIndicaDondeSeUsaCadaRol() {
        usuarioConRol(RolAcceso.SOLO_LECTURA);
        usarRolEnUnaLane();
        PageRequest pagina = PageRequest.of(0, 20);
        when(rolProcesoRepository.buscar(any(), eq(Boolean.TRUE), eq("emp"), eq(pagina)))
                .thenReturn(new PageImpl<>(List.of(rol)));

        Page<RolProcesoResponse> resultado = rolProcesoService.listar(USUARIO_ID, " emp ", false, pagina);

        RolProcesoResponse respuesta = resultado.getContent().get(0);
        assertThat(respuesta.getProcesosEnUso()).containsExactly("Vacaciones");
        assertThat(respuesta.isPuedeEliminarse()).isFalse();
    }
}
