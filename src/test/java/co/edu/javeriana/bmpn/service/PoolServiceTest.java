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
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.pool.AsociarRolPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.CrearPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.EditarPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.PermisoPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.PermisoPoolResponse;
import co.edu.javeriana.bmpn.dto.pool.PoolResponse;
import co.edu.javeriana.bmpn.dto.pool.RolPoolResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.PermisoPool;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.PoolRolDisponible;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.RolProceso;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.PoolRepository;

@ExtendWith(MockitoExtension.class)
class PoolServiceTest {

    private static final Long PROCESO_ID = 1L;
    private static final Long POOL_ID = 10L;
    private static final Long USUARIO_ID = 5L;
    private static final Long CLIENTE_ID = 2L;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private PoolRolDisponibleService poolRolDisponibleService;

    @Mock
    private PermisoPoolService permisoPoolService;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private ProcesoCompartidoService procesoCompartidoService;

    @Mock
    private RolProcesoService rolProcesoService;

    @Mock
    private HistorialProcesoService historialProcesoService;

    private PoolService poolService;

    private Empresa empresa;
    private Empresa cliente;
    private Proceso proceso;

    @BeforeEach
    void prepararDatos() {
        poolService = new PoolService(poolRepository, poolRolDisponibleService, permisoPoolService,
                empresaService, usuarioService, procesoService, procesoCompartidoService, rolProcesoService,
                historialProcesoService, new ModelMapperConfig().modelMapper());

        empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        cliente = new Empresa("800111222", "Cliente SA", "contacto@cliente.co");
        // En las pruebas no hay base de datos, asi que el id se asigna a mano
        ReflectionTestUtils.setField(empresa, "id", 1L);
        ReflectionTestUtils.setField(cliente, "id", CLIENTE_ID);
        proceso = new Proceso(empresa, "Vacaciones", "Solicitud", "RRHH");
        ReflectionTestUtils.setField(proceso, "id", PROCESO_ID);
    }

    private void usuarioYProceso(RolAcceso rolAcceso) {
        Usuario usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", rolAcceso);
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);
        when(procesoService.buscarActivoDeEmpresa(eq(PROCESO_ID), any())).thenReturn(proceso);
    }

    private CrearPoolRequest solicitud(String nombre, TipoParticipante tipo, Long empresaParticipanteId) {
        CrearPoolRequest request = new CrearPoolRequest();
        request.setNombre(nombre);
        request.setTipoParticipante(tipo);
        request.setEmpresaParticipanteId(empresaParticipanteId);
        return request;
    }

    @Test
    void crearPoolDeSistemaExterno() {
        usuarioYProceso(RolAcceso.EDITOR);
        CrearPoolRequest request = solicitud("Servicio de correo", TipoParticipante.SISTEMA_EXTERNO, null);
        request.setCajaNegra(true);
        request.setCanalExterno("CORREO");

        PoolResponse respuesta = poolService.crear(PROCESO_ID, USUARIO_ID, request);

        assertThat(respuesta.isCajaNegra()).isTrue();
        assertThat(respuesta.getCanalExterno()).isEqualTo("CORREO");
        verify(poolRepository).save(any(Pool.class));
    }

    @Test
    void crearPoolDeClienteInvitado() {
        usuarioYProceso(RolAcceso.EDITOR);
        when(empresaService.buscarActiva(CLIENTE_ID)).thenReturn(cliente);
        when(procesoCompartidoService.estaCompartidoCon(PROCESO_ID, CLIENTE_ID)).thenReturn(true);

        PoolResponse respuesta = poolService.crear(PROCESO_ID, USUARIO_ID,
                solicitud("Cliente", TipoParticipante.CLIENTE, CLIENTE_ID));

        assertThat(respuesta.getTipoParticipante()).isEqualTo(TipoParticipante.CLIENTE);
    }

    @Test
    void clienteNoInvitadoNoPuedeSerPool() {
        usuarioYProceso(RolAcceso.EDITOR);
        when(empresaService.buscarActiva(CLIENTE_ID)).thenReturn(cliente);
        when(procesoCompartidoService.estaCompartidoCon(PROCESO_ID, CLIENTE_ID)).thenReturn(false);

        assertThatThrownBy(() -> poolService.crear(PROCESO_ID, USUARIO_ID,
                solicitud("Cliente", TipoParticipante.CLIENTE, CLIENTE_ID)))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(poolRepository, never()).save(any());
    }

    @Test
    void noSeCreaOtroPoolPropietario() {
        usuarioYProceso(RolAcceso.EDITOR);

        assertThatThrownBy(() -> poolService.crear(PROCESO_ID, USUARIO_ID,
                solicitud("Otra empresa", TipoParticipante.EMPRESA_PROPIETARIA, null)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void nombreDePoolRepetido() {
        usuarioYProceso(RolAcceso.EDITOR);
        when(poolRepository.existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(PROCESO_ID, "Banco")).thenReturn(true);

        assertThatThrownBy(() -> poolService.crear(PROCESO_ID, USUARIO_ID,
                solicitud("Banco", TipoParticipante.SISTEMA_EXTERNO, null)))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void elPoolPropietarioNoSeElimina() {
        usuarioYProceso(RolAcceso.ADMINISTRADOR);
        Pool propietario = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        when(poolRepository.findByIdAndProcesoIdAndActivoTrue(POOL_ID, PROCESO_ID)).thenReturn(Optional.of(propietario));

        assertThatThrownBy(() -> poolService.eliminar(PROCESO_ID, POOL_ID, USUARIO_ID))
                .isInstanceOf(SolicitudInvalidaException.class);
        assertThat(propietario.isActivo()).isTrue();
    }

    @Test
    void eliminarPoolExterno() {
        usuarioYProceso(RolAcceso.ADMINISTRADOR);
        Pool externo = new Pool(null, "Banco", TipoParticipante.SISTEMA_EXTERNO, 1);
        when(poolRepository.findByIdAndProcesoIdAndActivoTrue(POOL_ID, PROCESO_ID)).thenReturn(Optional.of(externo));

        poolService.eliminar(PROCESO_ID, POOL_ID, USUARIO_ID);

        assertThat(externo.isActivo()).isFalse();
    }

    @Test
    void editarPool() {
        usuarioYProceso(RolAcceso.EDITOR);
        Pool externo = new Pool(null, "Banco", TipoParticipante.SISTEMA_EXTERNO, 1);
        when(poolRepository.findByIdAndProcesoIdAndActivoTrue(POOL_ID, PROCESO_ID)).thenReturn(Optional.of(externo));
        EditarPoolRequest request = new EditarPoolRequest();
        request.setNombre("Banco central");
        request.setCanalExterno("SERVICIO_WEB");
        request.setCajaNegra(true);

        PoolResponse respuesta = poolService.editar(PROCESO_ID, POOL_ID, USUARIO_ID, request);

        assertThat(respuesta.getNombre()).isEqualTo("Banco central");
        assertThat(externo.isCajaNegra()).isTrue();
    }

    @Test
    void asociarRolAlPool() {
        usuarioYProceso(RolAcceso.EDITOR);
        Pool propietario = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        RolProceso rol = new RolProceso(empresa, "Empleado", null);
        when(poolRepository.findByIdAndProcesoIdAndActivoTrue(POOL_ID, PROCESO_ID)).thenReturn(Optional.of(propietario));
        when(rolProcesoService.buscarActivo(eq(7L), any())).thenReturn(rol);
        when(poolRolDisponibleService.habilitar(propietario, rol)).thenReturn(new PoolRolDisponible(propietario, rol));

        RolPoolResponse respuesta = poolService.asociarRol(PROCESO_ID, POOL_ID, USUARIO_ID, new AsociarRolPoolRequest(7L));

        assertThat(respuesta.isActivo()).isTrue();
        verify(poolRolDisponibleService).habilitar(propietario, rol);
    }

    @Test
    void definirPermisoDelPool() {
        usuarioYProceso(RolAcceso.ADMINISTRADOR);
        Pool propietario = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        when(poolRepository.findByIdAndProcesoIdAndActivoTrue(POOL_ID, PROCESO_ID)).thenReturn(Optional.of(propietario));
        when(permisoPoolService.definir(propietario, RolAcceso.EDITOR, true, true, false))
                .thenReturn(new PermisoPool(propietario, RolAcceso.EDITOR, true, true, false));
        PermisoPoolRequest request = new PermisoPoolRequest();
        request.setRolAcceso(RolAcceso.EDITOR);
        request.setPuedeCrear(true);
        request.setPuedeEditar(true);

        PermisoPoolResponse respuesta = poolService.definirPermiso(PROCESO_ID, POOL_ID, USUARIO_ID, request);

        assertThat(respuesta.isPuedeCrear()).isTrue();
        assertThat(respuesta.isPuedeEliminar()).isFalse();
    }

    @Test
    void listarPools() {
        usuarioYProceso(RolAcceso.SOLO_LECTURA);
        when(poolRepository.findAllByProcesoIdAndActivoTrueOrderByOrdenAsc(PROCESO_ID))
                .thenReturn(List.of(new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0)));

        assertThat(poolService.listar(PROCESO_ID, USUARIO_ID)).hasSize(1);
    }
}
