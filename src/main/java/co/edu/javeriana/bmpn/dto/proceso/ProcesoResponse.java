package co.edu.javeriana.bmpn.dto.proceso;

import java.time.Instant;

import co.edu.javeriana.bmpn.entity.EstadoProceso;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;


public record ProcesoResponse(
        Long id,
        Long empresaId,
        String nombre,
        String descripcion,
        String categoria,
        EstadoProceso estado,
        Instant fechaCreacion,
        Instant fechaActualizacion,
        boolean activo,
        PoolPropietario poolPropietario) {

    public record PoolPropietario(Long id, String nombre, int orden) {
    }

    public static ProcesoResponse desde(Proceso proceso) {
        PoolPropietario pool = proceso.getPools().stream()
                .filter(Pool::esPropietario)
                .findFirst()
                .map(p -> new PoolPropietario(p.getId(), p.getNombre(), p.getOrden()))
                .orElse(null);

        return new ProcesoResponse(
                proceso.getId(),
                proceso.getEmpresa().getId(),
                proceso.getNombre(),
                proceso.getDescripcion(),
                proceso.getCategoria(),
                proceso.getEstado(),
                proceso.getFechaCreacion(),
                proceso.getFechaActualizacion(),
                proceso.isActivo(),
                pool);
    }
}