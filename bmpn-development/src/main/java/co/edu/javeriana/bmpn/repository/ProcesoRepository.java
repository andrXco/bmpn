package co.edu.javeriana.bmpn.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.EstadoProceso;
import co.edu.javeriana.bmpn.entity.Proceso;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    boolean existsByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);

    // Al editar se excluye el propio proceso para que no choque con su mismo nombre
    boolean existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(Long empresaId, String nombre, Long id);

    Optional<Proceso> findByIdAndEmpresaIdAndActivoTrue(Long id, Long empresaId);

    Optional<Proceso> findByIdAndEmpresaId(Long id, Long empresaId);

    // Si un filtro llega nulo, su condicion es verdadera y no se aplica
    @Query("SELECT p FROM Proceso p "
            + "WHERE p.empresa.id = :empresaId "
            + "AND (:activo IS NULL OR p.activo = :activo) "
            + "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) "
            + "AND (:estado IS NULL OR p.estado = :estado) "
            + "AND (:categoria IS NULL OR p.categoria = :categoria)")
    Page<Proceso> buscar(@Param("empresaId") Long empresaId,
                         @Param("activo") Boolean activo,
                         @Param("nombre") String nombre,
                         @Param("estado") EstadoProceso estado,
                         @Param("categoria") String categoria,
                         Pageable pageable);
}