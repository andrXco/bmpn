package co.edu.javeriana.bmpn.dto.empresa;

import java.time.Instant;

import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;

public record EmpresaResponse(
        Long id,
        String nit,
        String nombre,
        String correoContacto,
        Instant fechaCreacion,
        boolean activo,
        UsuarioResponse administradorInicial) {
}
