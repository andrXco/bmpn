package co.edu.javeriana.bmpn.dto.arco;

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
public class EditarArcoRequest {

    @NotNull(message = "El elemento de origen es obligatorio")
    private Long origenId;

    @NotNull(message = "El elemento de destino es obligatorio")
    private Long destinoId;

    @Size(max = 150, message = "La etiqueta no puede superar 150 caracteres")
    private String etiqueta;

    // Solo se permite si el arco sale de un gateway
    private String condicion;
}