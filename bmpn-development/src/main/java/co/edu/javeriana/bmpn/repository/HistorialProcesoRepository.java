package co.edu.javeriana.bmpn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.HistorialProceso;

public interface HistorialProcesoRepository extends JpaRepository<HistorialProceso, Long> {

    // Usa la named query "HistorialProceso.listarPorProceso" declarada en la entidad
    List<HistorialProceso> listarPorProceso(@Param("procesoId") Long procesoId);
}