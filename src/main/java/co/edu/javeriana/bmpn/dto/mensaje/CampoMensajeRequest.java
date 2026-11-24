package co.edu.javeriana.bmpn.dto.mensaje;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CampoMensajeRequest {

    @NotBlank(message = "El nombre del campo es obligatorio")
    @Size(max = 100, message = "El nombre del campo no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de dato del campo es obligatorio")
    @Size(max = 50, message = "El tipo de dato no puede superar 50 caracteres")
    private String tipoDato;

    @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
    private String descripcion;

    private boolean obligatorio;
}
