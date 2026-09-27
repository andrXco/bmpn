package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.procesocompartido.CompartirProcesoRequest;
import co.edu.javeriana.bmpn.dto.procesocompartido.ProcesoCompartidoResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.ProcesoCompartido;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.ProcesoCompartidoRepository;

@ExtendWith(MockitoExtension.class)
class ProcesoCompartidoServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long USUARIO_ID = 5L;
    private static final Long CLIENTE_ID = 2L;

    @Mock
    private ProcesoCompartidoRepository procesoCompartidoRepository;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    private ProcesoCompartidoService procesoCompartidoService;

    private Empresa empresa;
    private Empresa cliente;
    private Proceso proceso;
    private Usuario administrador;

    @BeforeEach
    void prepararDatos() {
        procesoCompartidoService = new ProcesoCompartidoService(procesoCompartidoRepository, empresaService,
                usuarioService, procesoService, historialProcesoService, new ModelMapperConfig().modelMapper());

        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        cliente = new Empresa("800111222", "Cliente SA", "contacto@cliente.co");
        // En las pruebas no hay base de datos, asi que el id se asigna a mano
        ReflectionTestUtils.setField(empresa, "id", 1L);
        ReflectionTestUtils.setField(cliente, "id", CLIENTE_ID);
        proceso = new Proceso(empresa, "Vacaciones", "Solicitud", "RRHH");
        administrador = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", RolAcceso.ADMINISTRADOR);
    }

    private void administradorYProceso() {
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(administrador);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
    }

    @Test
    void compartirProceso() {
        administradorYProceso();
        when(empresaService.buscarActiva(CLIENTE_ID)).thenReturn(cliente);
        when(procesoCompartidoRepository.findByProcesoIdAndEmpresaInvitadaId(PROCESO_ID, CLIENTE_ID))
                .thenReturn(Optional.empty());

        ProcesoCompartidoResponse respuesta = procesoCompartidoService.compartir(PROCESO_ID, USUARIO_ID,
                new CompartirProcesoRequest(CLIENTE_ID));

        assertThat(respuesta.getNombreEmpresaInvitada()).isEqualTo("Cliente SA");
        verify(procesoCompartidoRepository).save(any(ProcesoCompartido.class));
    }

    @Test
    void noSeComparteConLaMismaEmpresa() {
        administradorYProceso();
        when(empresaService.buscarActiva(1L)).thenReturn(empresa);

        assertThatThrownBy(() -> procesoCompartidoService.compartir(PROCESO_ID, USUARIO_ID,
                new CompartirProcesoRequest(1L)))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(procesoCompartidoRepository, never()).save(any());
    }

    @Test
    void empresaYaInvitada() {
        administradorYProceso();
        when(empresaService.buscarActiva(CLIENTE_ID)).thenReturn(cliente);
        when(procesoCompartidoRepository.findByProcesoIdAndEmpresaInvitadaId(PROCESO_ID, CLIENTE_ID))
                .thenReturn(Optional.of(new ProcesoCompartido(proceso, cliente, administrador)));

        assertThatThrownBy(() -> procesoCompartidoService.compartir(PROCESO_ID, USUARIO_ID,
                new CompartirProcesoRequest(CLIENTE_ID)))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void soloElAdministradorComparte() {
        Usuario editor = new Usuario(empresa, "luis@demo.co", "Luis", "Rojas", "hash", RolAcceso.EDITOR);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(editor);

        assertThatThrownBy(() -> procesoCompartidoService.compartir(PROCESO_ID, USUARIO_ID,
                new CompartirProcesoRequest(CLIENTE_ID)))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void revocarColaboracion() {
        administradorYProceso();
        ProcesoCompartido compartido = new ProcesoCompartido(proceso, cliente, administrador);
        when(procesoCompartidoRepository.findByIdAndProcesoIdAndActivoTrue(3L, PROCESO_ID))
                .thenReturn(Optional.of(compartido));

        procesoCompartidoService.revocar(PROCESO_ID, 3L, USUARIO_ID);

        assertThat(compartido.isActivo()).isFalse();
    }

    @Test
    void estaCompartidoSoloSiElRegistroEstaActivo() {
        ProcesoCompartido compartido = new ProcesoCompartido(proceso, cliente, administrador);
        compartido.desactivar();
        when(procesoCompartidoRepository.findByProcesoIdAndEmpresaInvitadaId(PROCESO_ID, CLIENTE_ID))
                .thenReturn(Optional.of(compartido));

        assertThat(procesoCompartidoService.estaCompartidoCon(PROCESO_ID, CLIENTE_ID)).isFalse();
    }
}
