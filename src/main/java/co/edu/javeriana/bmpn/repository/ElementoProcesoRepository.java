package co.edu.javeriana.bmpn.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.ElementoProceso;

public interface ElementoProcesoRepository extends JpaRepository<ElementoProceso, Long> {

    Optional<ElementoProceso> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);
}