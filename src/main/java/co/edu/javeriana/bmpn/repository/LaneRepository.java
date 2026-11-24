package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.Lane;

public interface LaneRepository extends JpaRepository<Lane, Long> {

    boolean existsByPoolIdAndRolProcesoId(Long poolId, Long rolProcesoId);

    boolean existsByPoolIdAndRolProcesoIdAndIdNot(Long poolId, Long rolProcesoId, Long id);

    Optional<Lane> findByIdAndPoolIdAndActivoTrue(Long id, Long poolId);

    List<Lane> findAllByPoolIdAndActivoTrueOrderByOrdenAsc(Long poolId);

    long countByPoolId(Long poolId);
}