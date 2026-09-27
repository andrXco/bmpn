package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.RolProceso;

public interface RolProcesoRepository extends JpaRepository<RolProceso, Long> {

    //un nombre desactivado sigue "reservado" (mismo criterio que ya usa ProcesoRepository con el nombre del proceso)
    boolean existsByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);

    boolean existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(Long empresaId, String nombre, Long id);

    Optional<RolProceso> findByIdAndEmpresaIdAndActivoTrue(Long id, Long empresaId);

    Optional<RolProceso> findByIdAndEmpresaId(Long id, Long empresaId);

    List<RolProceso> findAllByEmpresaIdAndActivoTrueOrderByNombreAsc(Long empresaId);

    List<RolProceso> findAllByEmpresaIdOrderByNombreAsc(Long empresaId);
}