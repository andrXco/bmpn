package co.edu.javeriana.bmpn.dto.actividad;

import java.math.BigDecimal;

import co.edu.javeriana.bmpn.entity.TipoActividad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearActividadRequest {

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    @Schema(description = "Nombre de la actividad.", example = "Revisar solicitud")
    private String nombre;

    @NotNull(message = "El tipo de actividad es obligatorio")
    @Schema(description = "Tipo BPMN de la actividad.", example = "TAREA")
    private TipoActividad tipoActividad;

    @NotNull(message = "La posicion X es obligatoria")
    @Digits(integer = 10, fraction = 2, message = "La posicion X admite hasta 10 enteros y 2 decimales")
    @Schema(description = "Coordenada horizontal en el diagrama.", example = "120.50")
    private BigDecimal posicionX;

    @NotNull(message = "La posicion Y es obligatoria")
    @Digits(integer = 10, fraction = 2, message = "La posicion Y admite hasta 10 enteros y 2 decimales")
    @Schema(description = "Coordenada vertical en el diagrama.", example = "80.00")
    private BigDecimal posicionY;

    @NotNull(message = "La lane es obligatoria")
    private Long laneId;

    // Opcional: si no se envia, la actividad queda en el pool de la empresa propietaria
    @Schema(description = "Identificador del pool BPMN; es opcional.", example = "1")
    private Long poolId;
}
