package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.Gateway;

public interface GatewayRepository extends JpaRepository<Gateway, Long> {

    Optional<Gateway> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    // Usa la named query "Gateway.listarActivosPorProceso" declarada en la entidad
    List<Gateway> listarActivosPorProceso(@Param("procesoId") Long procesoId);
}