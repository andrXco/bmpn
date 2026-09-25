package co.edu.javeriana.bmpn.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

// Clase padre de actividades, gateways y eventos
@Entity
@Table(name = "elemento_proceso")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ElementoProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "posicion_x", nullable = false, precision = 12, scale = 2)
    private BigDecimal posicionX;

    @Column(name = "posicion_y", nullable = false, precision = 12, scale = 2)
    private BigDecimal posicionY;

    @Column(nullable = false)
    private boolean activo;

    protected ElementoProceso(Proceso proceso, Pool pool, String nombre,
                              BigDecimal posicionX, BigDecimal posicionY) {
        this.proceso = proceso;
        this.pool = pool;
        this.nombre = nombre;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.activo = true;
    }

    public void renombrar(String nombre) {
        this.nombre = nombre;
    }

    public void mover(BigDecimal posicionX, BigDecimal posicionY) {
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }

    public void desactivar() {
        this.activo = false;
    }
}
