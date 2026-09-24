package co.edu.javeriana.bmpn.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "historial_proceso")
@NamedQuery(
        name = "HistorialProceso.listarPorProceso",
        query = "SELECT h FROM HistorialProceso h WHERE h.proceso.id = :procesoId ORDER BY h.fecha DESC")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HistorialProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AccionHistorial accion;

    @Column(columnDefinition = "text")
    private String detalle;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    public HistorialProceso(Proceso proceso, Usuario usuario, AccionHistorial accion, String detalle) {
        this.proceso = proceso;
        this.usuario = usuario;
        this.accion = accion;
        this.detalle = detalle;
        this.fecha = Instant.now();
    }
}