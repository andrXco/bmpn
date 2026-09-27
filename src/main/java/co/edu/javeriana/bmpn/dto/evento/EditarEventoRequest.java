package co.edu.javeriana.bmpn.dto.evento;

import java.math.BigDecimal;

import co.edu.javeriana.bmpn.entity.DisparadorEvento;
import co.edu.javeriana.bmpn.entity.TipoEvento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EditarEventoRequest {

    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de evento es obligatorio")
    private TipoEvento tipoEvento;

    @NotNull(message = "El disparador es obligatorio")
    private DisparadorEvento disparador;

    private boolean origenExterno;

    @NotNull(message = "La posicion X es obligatoria")
    @Digits(integer = 10, fraction = 2, message = "La posicion X admite hasta 10 enteros y 2 decimales")
    private BigDecimal posicionX;

    @NotNull(message = "La posicion Y es obligatoria")
    @Digits(integer = 10, fraction = 2, message = "La posicion Y admite hasta 10 enteros y 2 decimales")
    private BigDecimal posicionY;

    private Long laneId;
}
