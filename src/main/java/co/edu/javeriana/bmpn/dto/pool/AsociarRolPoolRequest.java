package co.edu.javeriana.bmpn.dto.pool;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsociarRolPoolRequest {

    @NotNull(message = "El rol de proceso es obligatorio")
    private Long rolProcesoId;
}
