package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.Gateway;

public interface GatewayRepository extends JpaRepository<Gateway, Long> {

    Optional<Gateway> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    @Query("SELECT g FROM Gateway g "
            + "WHERE g.proceso.id = :procesoId AND g.activo = true "
            + "ORDER BY g.id")
    List<Gateway> listarActivosPorProceso(@Param("procesoId") Long procesoId);
}