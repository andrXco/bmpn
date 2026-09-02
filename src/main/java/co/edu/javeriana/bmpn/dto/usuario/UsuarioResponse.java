package co.edu.javeriana.bmpn.dto.usuario;

import java.time.Instant;

import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;

public record UsuarioResponse(
        Long id,
        Long empresaId,
        String email,
        String nombre,
        String apellido,
        RolAcceso rolAcceso,
        Instant fechaCreacion,
        boolean activo) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmpresa().getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRolAcceso(),
                usuario.getFechaCreacion(),
                usuario.isActivo());
    }
}
