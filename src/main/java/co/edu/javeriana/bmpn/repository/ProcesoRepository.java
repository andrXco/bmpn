package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.Proceso;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    boolean existsByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);

    Optional<Proceso> findByIdAndEmpresaIdAndActivoTrue(Long id, Long empresaId);

    List<Proceso> findAllByEmpresaIdAndActivoTrueOrderByNombreAsc(Long empresaId);
}