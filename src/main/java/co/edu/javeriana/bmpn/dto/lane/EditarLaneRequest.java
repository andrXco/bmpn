package co.edu.javeriana.bmpn.dto.lane;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditarLaneRequest {

    @NotNull(message = "El rol de proceso de la lane es obligatorio")
    private Long rolProcesoId;
}
