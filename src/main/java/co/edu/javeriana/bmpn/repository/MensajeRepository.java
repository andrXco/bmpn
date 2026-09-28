package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.bmpn.entity.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    Optional<Mensaje> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    @Query("SELECT m FROM Mensaje m WHERE m.proceso.id = :procesoId AND m.activo = true ORDER BY m.id")
    List<Mensaje> listarActivosPorProceso(@Param("procesoId") Long procesoId);

    // HU-28: otro mensaje del proceso con el mismo nombre y la misma clave es ambiguo
    boolean existsByProcesoIdAndNombreIgnoreCaseAndClaveCorrelacionIgnoreCaseAndActivoTrueAndIdNot(
            Long procesoId, String nombre, String claveCorrelacion, Long id);

    //28 mensajes que comparten clave de correlacion dentro del mismo proceso
    @Query("SELECT m FROM Mensaje m WHERE m.proceso.id = :procesoId "
            + "AND m.claveCorrelacion = :clave AND m.activo = true ORDER BY m.id")
    List<Mensaje> listarPorCorrelacion(@Param("procesoId") Long procesoId, @Param("clave") String clave);
}
