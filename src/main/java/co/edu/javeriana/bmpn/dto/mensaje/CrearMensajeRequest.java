package co.edu.javeriana.bmpn.dto.mensaje;

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
public class CrearMensajeRequest {

    @NotNull(message = "El pool de origen es obligatorio")
    private Long poolOrigenId;

    @NotNull(message = "El pool de destino es obligatorio")
    private Long poolDestinoId;

    @NotBlank(message = "El nombre del mensaje es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    //28
    @NotBlank(message = "La clave de correlacion es obligatoria")
    @Size(max = 150, message = "La clave de correlacion no puede superar 150 caracteres")
    private String claveCorrelacion;

    @Size(max = 500, message = "La politica no puede superar 500 caracteres")
    private String politicaSinCorrespondencia;

    @Size(max = 500, message = "La politica no puede superar 500 caracteres")
    private String politicaFallo;
}
