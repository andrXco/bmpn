package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Optional<Evento> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    @Query("SELECT e FROM Evento e "
            + "WHERE e.proceso.id = :procesoId AND e.activo = true "
            + "ORDER BY e.id")
    List<Evento> listarActivosPorProceso(@Param("procesoId") Long procesoId);
}
