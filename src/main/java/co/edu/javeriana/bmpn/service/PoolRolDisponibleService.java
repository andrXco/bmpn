package co.edu.javeriana.bmpn.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.PoolRolDisponible;
import co.edu.javeriana.bmpn.entity.PoolRolDisponibleId;
import co.edu.javeriana.bmpn.entity.RolProceso;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.PoolRolDisponibleRepository;

// HU-24: roles de proceso que se pueden usar en las lanes de un pool
@Service
public class PoolRolDisponibleService {

    private final PoolRolDisponibleRepository poolRolDisponibleRepository;

    public PoolRolDisponibleService(PoolRolDisponibleRepository poolRolDisponibleRepository) {
        this.poolRolDisponibleRepository = poolRolDisponibleRepository;
    }

    @Transactional
    public PoolRolDisponible habilitar(Pool pool, RolProceso rol) {
        PoolRolDisponibleId id = new PoolRolDisponibleId(pool.getId(), rol.getId());
        PoolRolDisponible disponible = poolRolDisponibleRepository.findById(id).orElse(null);
        if (disponible == null) {
            disponible = new PoolRolDisponible(pool, rol);
            poolRolDisponibleRepository.save(disponible);
            return disponible;
        }
        if (disponible.isActivo()) {
            throw new RecursoDuplicadoException("Ese rol ya esta habilitado para el pool");
        }
        disponible.activar();
        return disponible;
    }

    @Transactional
    public void deshabilitar(Long poolId, Long rolProcesoId) {
        PoolRolDisponible disponible = poolRolDisponibleRepository
                .findById(new PoolRolDisponibleId(poolId, rolProcesoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("El rol no esta habilitado para el pool"));
        disponible.desactivar();
    }

    @Transactional(readOnly = true)
    public List<PoolRolDisponible> listarActivos(Long poolId) {
        return poolRolDisponibleRepository.listarActivosPorPool(poolId);
    }
}
