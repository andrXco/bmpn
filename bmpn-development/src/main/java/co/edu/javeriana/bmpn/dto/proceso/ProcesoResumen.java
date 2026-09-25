package co.edu.javeriana.bmpn.dto.proceso;

import java.time.Instant;

import co.edu.javeriana.bmpn.entity.EstadoProceso;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcesoResumen {

    private Long id;
    private String nombre;
    private String categoria;
    private EstadoProceso estado;
    private boolean activo;
    private Instant fechaActualizacion;
}