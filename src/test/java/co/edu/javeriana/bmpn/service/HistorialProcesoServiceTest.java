package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.javeriana.bmpn.config.ModelMapperConfig;
import co.edu.javeriana.bmpn.dto.proceso.HistorialResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.HistorialProceso;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.repository.HistorialProcesoRepository;

@ExtendWith(MockitoExtension.class)
class HistorialProcesoServiceTest {

    private static final Long PROCESO_ID = 1L;

    @Mock
    private HistorialProcesoRepository historialProcesoRepository;

    private HistorialProcesoService historialProcesoService;

    private Proceso proceso;
    private Usuario usuario;

    @BeforeEach
    void prepararDatos() {
        historialProcesoService = new HistorialProcesoService(historialProcesoRepository,
                new ModelMapperConfig().modelMapper());

        Empresa empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        usuario = new Usuario(empresa, "ana@demo.co", "Ana", "Paz", "hash", RolAcceso.EDITOR);
        proceso = new Proceso(empresa, "Solicitud de vacaciones", "Proceso de ejemplo", "RRHH");
    }

    @Test
    void registrarHistorial() {
        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION, "Proceso creado");

        ArgumentCaptor<HistorialProceso> guardado = ArgumentCaptor.forClass(HistorialProceso.class);
        verify(historialProcesoRepository).save(guardado.capture());
        assertThat(guardado.getValue().getProceso()).isEqualTo(proceso);
        assertThat(guardado.getValue().getUsuario()).isEqualTo(usuario);
        assertThat(guardado.getValue().getAccion()).isEqualTo(AccionHistorial.CREACION);
        assertThat(guardado.getValue().getFecha()).isNotNull();
    }

    @Test
    void listarPorProceso() {
        HistorialProceso registro = new HistorialProceso(proceso, usuario,
                AccionHistorial.ACTUALIZACION, "Proceso actualizado");
        when(historialProcesoRepository.listarPorProceso(PROCESO_ID)).thenReturn(List.of(registro));

        List<HistorialResponse> historial = historialProcesoService.listarPorProceso(PROCESO_ID);

        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).getAccion()).isEqualTo(AccionHistorial.ACTUALIZACION);
        assertThat(historial.get(0).getDetalle()).isEqualTo("Proceso actualizado");
        assertThat(historial.get(0).getNombreUsuario()).isEqualTo("Ana Paz");
        assertThat(historial.get(0).getEmailUsuario()).isEqualTo("ana@demo.co");
    }

    @Test
    void listarSinRegistros() {
        when(historialProcesoRepository.listarPorProceso(PROCESO_ID)).thenReturn(List.of());

        assertThat(historialProcesoService.listarPorProceso(PROCESO_ID)).isEmpty();
    }
}
