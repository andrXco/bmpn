package co.edu.javeriana.bmpn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.PoolRolDisponible;
import co.edu.javeriana.bmpn.entity.PoolRolDisponibleId;

public interface PoolRolDisponibleRepository
        extends JpaRepository<PoolRolDisponible, PoolRolDisponibleId> {

    //detectar existencia
    @Query("SELECT prd FROM PoolRolDisponible prd WHERE prd.pool.id = :poolId AND prd.activo = true")
    List<PoolRolDisponible> listarActivosPorPool(@Param("poolId") Long poolId);
}
