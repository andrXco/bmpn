package co.edu.javeriana.bmpn.dto.autenticacion;

import java.io.Serializable;

import co.edu.javeriana.bmpn.entity.RolAcceso;

public record SesionUsuarioResponse(
        Long usuarioId,
        Long empresaId,
        String email,
        String nombreCompleto,
        RolAcceso rolAcceso) implements Serializable {

    private static final long serialVersionUID = 1L;
}
