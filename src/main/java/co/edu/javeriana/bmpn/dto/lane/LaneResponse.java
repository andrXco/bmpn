package co.edu.javeriana.bmpn.dto.lane;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LaneResponse {
    private Long id;
    private Long poolId;
    private Long rolProcesoId;
    private String nombreRol;
    private int orden;
    private boolean activo;
}
