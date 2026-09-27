package co.edu.javeriana.bmpn.dto.pool;

import co.edu.javeriana.bmpn.entity.RolAcceso;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PermisoPoolResponse {
    private Long id;
    private Long poolId;
    private RolAcceso rolAcceso;
    private boolean puedeCrear;
    private boolean puedeEditar;
    private boolean puedeEliminar;
}
