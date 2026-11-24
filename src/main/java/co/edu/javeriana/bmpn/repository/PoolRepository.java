package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.Pool;

public interface PoolRepository extends JpaRepository<Pool, Long> {

    boolean existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(Long procesoId, String nombre);

    boolean existsByProcesoIdAndNombreIgnoreCaseAndActivoTrueAndIdNot(Long procesoId, String nombre, Long id);

    Optional<Pool> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    Optional<Pool> findByIdAndProcesoId(Long id, Long procesoId);

    List<Pool> findAllByProcesoIdAndActivoTrueOrderByOrdenAsc(Long procesoId);

    long countByProcesoId(Long procesoId);
}