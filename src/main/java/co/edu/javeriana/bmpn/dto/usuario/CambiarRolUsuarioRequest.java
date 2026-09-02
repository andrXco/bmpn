package co.edu.javeriana.bmpn.dto.usuario;

import co.edu.javeriana.bmpn.entity.RolAcceso;
import jakarta.validation.constraints.NotNull;

public record CambiarRolUsuarioRequest(
        @NotNull(message = "El nuevo rol es obligatorio")
        RolAcceso rolAcceso) {
}
