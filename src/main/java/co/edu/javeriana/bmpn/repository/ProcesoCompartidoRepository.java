package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.ProcesoCompartido;

public interface ProcesoCompartidoRepository extends JpaRepository<ProcesoCompartido, Long> {

    //si ya existe empresa con proceso(activo o no) hay que reactivar, no insertar de nuevo
    Optional<ProcesoCompartido> findByProcesoIdAndEmpresaInvitadaId(Long procesoId, Long empresaInvitadaId);

    Optional<ProcesoCompartido> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    List<ProcesoCompartido> findAllByProcesoIdAndActivoTrue(Long procesoId);
}