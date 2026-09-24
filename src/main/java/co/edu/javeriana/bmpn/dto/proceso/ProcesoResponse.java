package co.edu.javeriana.bmpn.dto.proceso;

import java.time.Instant;

import co.edu.javeriana.bmpn.entity.EstadoProceso;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcesoResponse {

    private Long id;
    private Long empresaId;
    private String nombre;
    private String descripcion;
    private String categoria;
    private EstadoProceso estado;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;
    private boolean activo;
    private Long poolPropietarioId;
    private String poolPropietarioNombre;
}