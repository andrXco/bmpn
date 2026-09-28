package co.edu.javeriana.bmpn.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.RolProceso;

public interface RolProcesoRepository extends JpaRepository<RolProceso, Long> {

    //un nombre desactivado sigue "reservado" (mismo criterio que ya usa ProcesoRepository con el nombre del proceso)
    boolean existsByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);

    boolean existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(Long empresaId, String nombre, Long id);

    Optional<RolProceso> findByIdAndEmpresaIdAndActivoTrue(Long id, Long empresaId);

    Optional<RolProceso> findByIdAndEmpresaId(Long id, Long empresaId);

    // Sin nombre se busca con texto vacio, asi el LIKE '%%' trae todos los roles
    @Query("SELECT r FROM RolProceso r "
            + "WHERE r.empresa.id = :empresaId "
            + "AND (:activo IS NULL OR r.activo = :activo) "
            + "AND LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<RolProceso> buscar(@Param("empresaId") Long empresaId,
                            @Param("activo") Boolean activo,
                            @Param("nombre") String nombre,
                            Pageable pageable);
}