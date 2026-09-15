package co.edu.javeriana.bmpn.dto.proceso;

import java.time.Instant;

import co.edu.javeriana.bmpn.entity.EstadoProceso;

public record ProcesoResumen(
        Long id,
        String nombre,
        String categoria,
        EstadoProceso estado,
        boolean activo,
        Instant fechaActualizacion) {
}