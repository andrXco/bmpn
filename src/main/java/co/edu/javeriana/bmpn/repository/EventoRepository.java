package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Optional<Evento> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    // Usa la named query "Evento.listarActivosPorProceso" declarada en la entidad
    List<Evento> listarActivosPorProceso(@Param("procesoId") Long procesoId);
}
