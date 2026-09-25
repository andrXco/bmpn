package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.Actividad;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    boolean existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(Long procesoId, String nombre);

    // Al editar se excluye la propia actividad para que no choque con su mismo nombre
    boolean existsByProcesoIdAndNombreIgnoreCaseAndActivoTrueAndIdNot(
            Long procesoId, String nombre, Long id);

    Optional<Actividad> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    @Query("SELECT a FROM Actividad a "
            + "WHERE a.proceso.id = :procesoId AND a.activo = true "
            + "ORDER BY a.nombre")
    List<Actividad> listarActivasPorProceso(@Param("procesoId") Long procesoId);
}
