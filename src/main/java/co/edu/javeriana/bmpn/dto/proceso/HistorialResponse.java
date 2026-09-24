package co.edu.javeriana.bmpn.dto.proceso;

import java.time.Instant;

import co.edu.javeriana.bmpn.entity.AccionHistorial;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HistorialResponse {

    private Long id;
    private AccionHistorial accion;
    private String detalle;
    private Instant fecha;
    private String nombreUsuario;
    private String emailUsuario;
}