package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.Arco;

public interface ArcoRepository extends JpaRepository<Arco, Long> {

    // Sin filtrar por activo: tambien encuentra un arco eliminado logicamente
    Optional<Arco> findByProcesoIdAndOrigenIdAndDestinoId(Long procesoId, Long origenId, Long destinoId);

    Optional<Arco> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    @Query("SELECT a FROM Arco a "
            + "WHERE a.proceso.id = :procesoId AND a.activo = true "
            + "ORDER BY a.id")
    List<Arco> listarActivosPorProceso(@Param("procesoId") Long procesoId);
}