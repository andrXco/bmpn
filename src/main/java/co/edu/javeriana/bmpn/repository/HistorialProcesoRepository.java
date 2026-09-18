package co.edu.javeriana.bmpn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.HistorialProceso;

public interface HistorialProcesoRepository extends JpaRepository<HistorialProceso, Long> {

    List<HistorialProceso> findAllByProcesoIdOrderByFechaDesc(Long procesoId);
}