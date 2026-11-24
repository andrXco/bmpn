package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.javeriana.bmpn.entity.Actividad;
import co.edu.javeriana.bmpn.entity.ElementoProceso;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.TipoActividad;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.ElementoProcesoRepository;

@ExtendWith(MockitoExtension.class)
class ElementoProcesoServiceTest {

    private static final Long PROCESO_ID = 1L;

    @Mock
    private ElementoProcesoRepository elementoProcesoRepository;

    private ElementoProcesoService elementoProcesoService;

    private Proceso proceso;
    private Pool poolEmpresa;
    private Pool poolCliente;

    @BeforeEach
    void prepararDatos() {
        elementoProcesoService = new ElementoProcesoService(elementoProcesoRepository);

        Empresa empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        proceso = new Proceso(empresa, "Solicitud de vacaciones", "Proceso de ejemplo", "RRHH");
        poolEmpresa = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        poolCliente = new Pool(null, "Cliente", TipoParticipante.CLIENTE, 1);
        asignarId(poolEmpresa, 10L);
        asignarId(poolCliente, 20L);
        proceso.agregarPool(poolEmpresa);
        proceso.agregarPool(poolCliente);
    }

    // En las pruebas no hay base de datos, asi que el id se asigna a mano
    private void asignarId(Object entidad, Long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
    }

    @Test
    void buscarElementoDelProceso() {
        Actividad actividad = new Actividad(proceso, poolEmpresa, "Radicar solicitud",
                TipoActividad.USUARIO, BigDecimal.ONE, BigDecimal.ONE);
        when(elementoProcesoRepository.findByIdAndProcesoIdAndActivoTrue(5L, PROCESO_ID))
                .thenReturn(Optional.of(actividad));

        ElementoProceso encontrado = elementoProcesoService.buscarActivoDelProceso(5L, PROCESO_ID);

        assertThat(encontrado).isEqualTo(actividad);
    }

    @Test
    void buscarElementoInexistente() {
        when(elementoProcesoRepository.findByIdAndProcesoIdAndActivoTrue(5L, PROCESO_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> elementoProcesoService.buscarActivoDelProceso(5L, PROCESO_ID))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void poolPorDefectoEsElDeLaEmpresa() {
        assertThat(elementoProcesoService.buscarPoolParaElemento(proceso, null)).isEqualTo(poolEmpresa);
    }

    @Test
    void buscarPoolPorId() {
        assertThat(elementoProcesoService.buscarPoolParaElemento(proceso, 20L)).isEqualTo(poolCliente);
    }

    @Test
    void poolInexistente() {
        assertThatThrownBy(() -> elementoProcesoService.buscarPoolParaElemento(proceso, 99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void poolCajaNegraNoAceptaElementos() {
        ReflectionTestUtils.setField(poolCliente, "cajaNegra", true);

        assertThatThrownBy(() -> elementoProcesoService.buscarPoolParaElemento(proceso, 20L))
                .isInstanceOf(SolicitudInvalidaException.class);
    }
}
