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

    // El elemento todavia tiene algun camino de salida
    boolean existsByOrigenIdAndActivoTrue(Long origenId);

    // El elemento todavia tiene algun camino de entrada
    boolean existsByDestinoIdAndActivoTrue(Long destinoId);

    // Arcos activos que entran o salen de un elemento
    @Query("SELECT a FROM Arco a "
            + "WHERE a.activo = true "
            + "AND (a.origen.id = :elementoId OR a.destino.id = :elementoId)")
    List<Arco> listarActivosDeElemento(@Param("elementoId") Long elementoId);

    // Arcos activos que entran o salen de algun elemento del pool
    @Query("SELECT a FROM Arco a "
            + "WHERE a.activo = true "
            + "AND (a.origen.pool.id = :poolId OR a.destino.pool.id = :poolId)")
    List<Arco> listarActivosDePool(@Param("poolId") Long poolId);

    // Arcos que salen de un elemento, para revisar las salidas de un gateway
    List<Arco> findByOrigenIdAndActivoTrue(Long origenId);

    long countByDestinoIdAndActivoTrue(Long destinoId);
}