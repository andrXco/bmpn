package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.PoolRolDisponible;
import co.edu.javeriana.bmpn.entity.RolProceso;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.PoolRolDisponibleRepository;

@ExtendWith(MockitoExtension.class)
class PoolRolDisponibleServiceTest {

    @Mock
    private PoolRolDisponibleRepository poolRolDisponibleRepository;

    private PoolRolDisponibleService poolRolDisponibleService;

    private Pool pool;
    private RolProceso rol;

    @BeforeEach
    void prepararDatos() {
        poolRolDisponibleService = new PoolRolDisponibleService(poolRolDisponibleRepository);
        Empresa empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        pool = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
        rol = new RolProceso(empresa, "Empleado", null);
    }

    @Test
    void habilitarRol() {
        when(poolRolDisponibleRepository.findById(any())).thenReturn(Optional.empty());

        PoolRolDisponible disponible = poolRolDisponibleService.habilitar(pool, rol);

        assertThat(disponible.isActivo()).isTrue();
        verify(poolRolDisponibleRepository).save(any(PoolRolDisponible.class));
    }

    @Test
    void habilitarRolYaHabilitado() {
        when(poolRolDisponibleRepository.findById(any())).thenReturn(Optional.of(new PoolRolDisponible(pool, rol)));

        assertThatThrownBy(() -> poolRolDisponibleService.habilitar(pool, rol))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void volverAHabilitarUnRolDeshabilitado() {
        PoolRolDisponible deshabilitado = new PoolRolDisponible(pool, rol);
        deshabilitado.desactivar();
        when(poolRolDisponibleRepository.findById(any())).thenReturn(Optional.of(deshabilitado));

        poolRolDisponibleService.habilitar(pool, rol);

        assertThat(deshabilitado.isActivo()).isTrue();
        verify(poolRolDisponibleRepository, never()).save(any());
    }

    @Test
    void deshabilitarRolQueNoEstaHabilitado() {
        when(poolRolDisponibleRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> poolRolDisponibleService.deshabilitar(1L, 2L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
