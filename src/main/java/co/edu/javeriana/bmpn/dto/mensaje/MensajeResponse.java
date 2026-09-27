package co.edu.javeriana.bmpn.dto.mensaje;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MensajeResponse {
    private Long id;
    private Long procesoId;
    private Long poolOrigenId;
    private Long poolDestinoId;
    private String nombre;
    private String claveCorrelacion;
    private String politicaSinCorrespondencia;
    private String politicaFallo;
    private boolean notificacionExterna;
    private boolean activo;
}
