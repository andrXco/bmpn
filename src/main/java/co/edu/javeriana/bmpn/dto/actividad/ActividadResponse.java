package co.edu.javeriana.bmpn.dto.actividad;

import java.math.BigDecimal;

import co.edu.javeriana.bmpn.entity.TipoActividad;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActividadResponse {

    private Long id;
    private Long procesoId;
    private Long poolId;
    private String nombre;
    private TipoActividad tipoActividad;
    private BigDecimal posicionX;
    private BigDecimal posicionY;
    private boolean activo;
}
