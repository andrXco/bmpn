package co.edu.javeriana.bmpn.dto.gateway;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.edu.javeriana.bmpn.entity.TipoGateway;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GatewayResponse {

    private Long id;
    private Long procesoId;
    private Long poolId;
    private String nombre;
    private TipoGateway tipoGateway;
    private BigDecimal posicionX;
    private BigDecimal posicionY;
    private boolean activo;
    private List<String> advertencias = new ArrayList<>();
}