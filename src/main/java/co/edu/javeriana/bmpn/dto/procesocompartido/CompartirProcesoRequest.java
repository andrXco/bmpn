package co.edu.javeriana.bmpn.dto.procesocompartido;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompartirProcesoRequest {

    @NotNull(message = "La empresa invitada es obligatoria")
    private Long empresaInvitadaId;
}
