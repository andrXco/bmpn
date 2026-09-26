package co.edu.javeriana.bmpn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "arco")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Arco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origen_id", nullable = false)
    private ElementoProceso origen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private ElementoProceso destino;

    @Column(length = 150)
    private String etiqueta;

    @Column(columnDefinition = "text")
    private String condicion;

    @Column(nullable = false)
    private boolean activo;

    public Arco(Proceso proceso, ElementoProceso origen, ElementoProceso destino,
                String etiqueta, String condicion) {
        this.proceso = proceso;
        this.origen = origen;
        this.destino = destino;
        this.etiqueta = etiqueta;
        this.condicion = condicion;
        this.activo = true;
    }

    // Se usa cuando se vuelve a crear un arco que habia sido eliminado logicamente
    public void reactivar(String etiqueta, String condicion) {
        this.etiqueta = etiqueta;
        this.condicion = condicion;
        this.activo = true;
    }

    public void actualizar(ElementoProceso origen, ElementoProceso destino,
                           String etiqueta, String condicion) {
        this.origen = origen;
        this.destino = destino;
        this.etiqueta = etiqueta;
        this.condicion = condicion;
    }

    public void desactivar() {
        this.activo = false;
    }
}