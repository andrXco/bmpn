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
import co.edu.javeriana.bmpn.entity.PermisoPool;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.PermisoPoolRepository;

@ExtendWith(MockitoExtension.class)
class PermisoPoolServiceTest {

    @Mock
    private PermisoPoolRepository permisoPoolRepository;

    private PermisoPoolService permisoPoolService;

    private Pool pool;

    @BeforeEach
    void prepararDatos() {
        permisoPoolService = new PermisoPoolService(permisoPoolRepository);
        Empresa empresa = new Empresa("900123456", "Empresa Demo", "contacto@demo.co");
        pool = new Pool(empresa, "Empresa Demo", TipoParticipante.EMPRESA_PROPIETARIA, 0);
    }

    @Test
    void definirPermisoNuevo() {
        when(permisoPoolRepository.buscarPorPoolYRol(any(), any())).thenReturn(Optional.empty());

        PermisoPool permiso = permisoPoolService.definir(pool, RolAcceso.EDITOR, true, true, false);

        assertThat(permiso.isPuedeCrear()).isTrue();
        assertThat(permiso.isPuedeEliminar()).isFalse();
        verify(permisoPoolRepository).save(any(PermisoPool.class));
    }

    @Test
    void actualizarPermisoExistente() {
        PermisoPool existente = new PermisoPool(pool, RolAcceso.EDITOR, false, false, false);
        when(permisoPoolRepository.buscarPorPoolYRol(any(), any())).thenReturn(Optional.of(existente));

        permisoPoolService.definir(pool, RolAcceso.EDITOR, true, true, true);

        assertThat(existente.isPuedeEliminar()).isTrue();
        verify(permisoPoolRepository, never()).save(any());
    }

    @Test
    void soloLecturaNoPuedeModificar() {
        assertThatThrownBy(() -> permisoPoolService.definir(pool, RolAcceso.SOLO_LECTURA, true, false, false))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(permisoPoolRepository, never()).save(any());
    }
}
