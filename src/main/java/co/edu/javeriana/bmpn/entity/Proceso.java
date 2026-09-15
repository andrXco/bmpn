package co.edu.javeriana.bmpn.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "proceso")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProceso estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    @Column(nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "proceso", fetch = FetchType.LAZY,
               cascade = CascadeType.PERSIST)
    private Set<Pool> pools = new HashSet<>();

    public Proceso(Empresa empresa, String nombre, String descripcion, String categoria) {
        this.empresa = empresa;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.estado = EstadoProceso.BORRADOR;  
        this.activo = true;
    }

    // Mantiene sincronizados ambos lados de la relación
    public void agregarPool(Pool pool) {
        pools.add(pool);
        pool.asignarProceso(this);
    }

    public void publicar() {
        this.estado = EstadoProceso.PUBLICADO;
    }

    public void actualizarDatos(
            String nombre, String descripcion, String categoria, EstadoProceso estado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.estado = estado;
    }

    public void desactivar() {
        this.activo = false;
    }

    @PrePersist
    void alCrear() {
        Instant ahora = Instant.now();
        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }
        fechaActualizacion = ahora;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = Instant.now();
    }
}