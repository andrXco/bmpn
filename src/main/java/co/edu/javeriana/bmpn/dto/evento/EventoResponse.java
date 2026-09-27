package co.edu.javeriana.bmpn.dto.evento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.edu.javeriana.bmpn.entity.DisparadorEvento;
import co.edu.javeriana.bmpn.entity.TipoEvento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventoResponse {

    private Long id;
    private Long procesoId;
    private Long poolId;
    private Long laneId;
    private String nombre;
    private TipoEvento tipoEvento;
    private DisparadorEvento disparador;
    private boolean origenExterno;
    private BigDecimal posicionX;
    private BigDecimal posicionY;
    private boolean activo;
    private List<String> advertencias = new ArrayList<>();
}
