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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_acceso", nullable = false, length = 30)
    private RolAcceso rolAcceso;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(nullable = false)
    private boolean activo;

    protected Usuario() {
    }

    public Usuario(
            Empresa empresa,
            String email,
            String nombre,
            String apellido,
            String passwordHash,
            RolAcceso rolAcceso) {
        this.empresa = empresa;
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.passwordHash = passwordHash;
        this.rolAcceso = rolAcceso;
        this.activo = true;
    }

    public void cambiarRol(RolAcceso nuevoRol) {
        this.rolAcceso = nuevoRol;
    }

    public void actualizarPasswordHash(String nuevoPasswordHash) {
        this.passwordHash = nuevoPasswordHash;
    }

    public void desactivar() {
        this.activo = false;
    }

    public boolean puedeEditar() {
        return activo && rolAcceso != RolAcceso.SOLO_LECTURA;
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public RolAcceso getRolAcceso() {
        return rolAcceso;
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
