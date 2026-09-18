package co.edu.javeriana.bmpn.dto.proceso;

import java.time.Instant;

import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.HistorialProceso;

public record HistorialResponse(
        Long id,
        AccionHistorial accion,
        String detalle,
        Instant fecha,
        String nombreUsuario,
        String emailUsuario) {

    public static HistorialResponse desde(HistorialProceso historial) {
        return new HistorialResponse(
                historial.getId(),
                historial.getAccion(),
                historial.getDetalle(),
                historial.getFecha(),
                historial.getUsuario().getNombre() + " " + historial.getUsuario().getApellido(),
                historial.getUsuario().getEmail());
    }
}