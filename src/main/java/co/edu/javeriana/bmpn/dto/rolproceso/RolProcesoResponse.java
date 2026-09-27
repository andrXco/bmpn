package co.edu.javeriana.bmpn.dto.rolproceso;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolProcesoResponse {
    private Long id;
    private Long empresaId;
    private String nombre;
    private String descripcion;
    private boolean activo;
}
