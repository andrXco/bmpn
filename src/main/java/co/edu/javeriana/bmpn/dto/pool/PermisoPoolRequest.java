package co.edu.javeriana.bmpn.dto.pool;

import co.edu.javeriana.bmpn.entity.RolAcceso;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermisoPoolRequest {

    @NotNull(message = "El rol de acceso es obligatorio")
    private RolAcceso rolAcceso;

    private boolean puedeCrear;
    private boolean puedeEditar;
    private boolean puedeEliminar;
}
