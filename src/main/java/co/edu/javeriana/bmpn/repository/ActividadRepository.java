package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.Actividad;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    boolean existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(Long procesoId, String nombre);

    // Al editar se excluye la propia actividad para que no choque con su mismo nombre
    boolean existsByProcesoIdAndNombreIgnoreCaseAndActivoTrueAndIdNot(
            Long procesoId, String nombre, Long id);

    Optional<Actividad> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    // Usa la named query "Actividad.listarActivasPorProceso" declarada en la entidad
    List<Actividad> listarActivasPorProceso(@Param("procesoId") Long procesoId);
}
