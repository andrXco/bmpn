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

    // HU-23: la empresa puede leer sus procesos activos y los que otra empresa le compartio
    @Query("SELECT p FROM Proceso p "
            + "WHERE p.id = :id AND p.activo = true "
            + "AND (p.empresa.id = :empresaId OR EXISTS (SELECT c FROM ProcesoCompartido c "
            + "WHERE c.proceso = p AND c.empresaInvitada.id = :empresaId AND c.activo = true))")
    Optional<Proceso> buscarVisible(@Param("id") Long id, @Param("empresaId") Long empresaId);

    // Si un filtro llega nulo, su condicion es verdadera y no se aplica.
    // Tambien trae los procesos activos compartidos con la empresa (HU-23)
    @Query("SELECT p FROM Proceso p "
            + "WHERE (p.empresa.id = :empresaId OR (p.activo = true AND EXISTS (SELECT c FROM ProcesoCompartido c "
            + "WHERE c.proceso = p AND c.empresaInvitada.id = :empresaId AND c.activo = true))) "
            + "AND (:activo IS NULL OR p.activo = :activo) "
            + "AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) "
            + "AND (:estado IS NULL OR p.estado = :estado) "
            + "AND (:categoria IS NULL OR p.categoria = :categoria)")
    Page<Proceso> buscar(@Param("empresaId") Long empresaId,
                         @Param("activo") Boolean activo,
                         @Param("nombre") String nombre,
                         @Param("estado") EstadoProceso estado,
                         @Param("categoria") String categoria,
                         Pageable pageable);
}