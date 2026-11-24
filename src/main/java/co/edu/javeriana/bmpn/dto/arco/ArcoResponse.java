package co.edu.javeriana.bmpn.dto.arco;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArcoResponse {

    private Long id;
    private Long procesoId;
    private Long origenId;
    private Long destinoId;
    private String etiqueta;
    private String condicion;
    private boolean activo;
}