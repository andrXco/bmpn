package co.edu.javeriana.bmpn.dto.rolproceso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditarRolProcesoRequest {

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
    private String descripcion;
}
