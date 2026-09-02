package co.edu.javeriana.bmpn.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nit;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "correo_contacto", nullable = false, length = 254)
    private String correoContacto;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(nullable = false)
    private boolean activo;

    protected Empresa() {
    }

    public Empresa(String nit, String nombre, String correoContacto) {
        this.nit = nit;
        this.nombre = nombre;
        this.correoContacto = correoContacto;
        this.activo = true;
    }

    public void actualizarDatos(String nombre, String correoContacto) {
        this.nombre = nombre;
        this.correoContacto = correoContacto;
    }

    public void desactivar() {
        this.activo = false;
    }

    public Long getId() {
        return id;
    }

    public String getNit() {
        return nit;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    @PrePersist
    void asignarFechaCreacion() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
    }
}
