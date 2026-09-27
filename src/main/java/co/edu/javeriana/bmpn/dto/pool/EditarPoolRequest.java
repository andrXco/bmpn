package co.edu.javeriana.bmpn.dto.pool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EditarPoolRequest {

    @NotBlank(message = "El nombre del pool es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 100, message = "El canal externo no puede superar 100 caracteres")
    private String canalExterno;

    private boolean cajaNegra;
}
