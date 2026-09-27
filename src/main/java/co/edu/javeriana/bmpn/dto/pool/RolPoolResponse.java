package co.edu.javeriana.bmpn.dto.pool;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolPoolResponse {
    private Long poolId;
    private Long rolProcesoId;
    private String nombreRol;
    private boolean activo;
}
