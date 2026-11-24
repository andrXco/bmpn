package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.PermisoPool;
import co.edu.javeriana.bmpn.entity.RolAcceso;

public interface PermisoPoolRepository extends JpaRepository<PermisoPool, Long> {

    @Query("SELECT pp FROM PermisoPool pp WHERE pp.pool.id = :poolId")
    List<PermisoPool> listarPorPool(@Param("poolId") Long poolId);

    @Query("SELECT pp FROM PermisoPool pp WHERE pp.pool.id = :poolId AND pp.rolAcceso = :rolAcceso")
    Optional<PermisoPool> buscarPorPoolYRol(
            @Param("poolId") Long poolId, @Param("rolAcceso") RolAcceso rolAcceso);
}