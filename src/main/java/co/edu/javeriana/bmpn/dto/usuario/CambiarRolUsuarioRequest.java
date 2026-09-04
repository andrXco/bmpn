package co.edu.javeriana.bmpn.dto.usuario;

import co.edu.javeriana.bmpn.entity.RolAcceso;
import jakarta.validation.constraints.NotNull;

public class CambiarRolUsuarioRequest {
    @NotNull(message = "El nuevo rol es obligatorio")
    private RolAcceso rolAcceso;

    public CambiarRolUsuarioRequest() {
    }

    public CambiarRolUsuarioRequest(RolAcceso rolAcceso) {
        this.rolAcceso = rolAcceso;
    }

    public RolAcceso getRolAcceso() {
        return rolAcceso;
    }

    public void setRolAcceso(RolAcceso rolAcceso) {
        this.rolAcceso = rolAcceso;
    }
}
