package co.edu.javeriana.bmpn.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "familia_id", nullable = false)
    private UUID familiaId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private Instant fechaExpiracion;

    @Column(name = "fecha_revocacion")
    private Instant fechaRevocacion;

    protected RefreshToken() {
    }

    public RefreshToken(Usuario usuario, String tokenHash, UUID familiaId, Instant fechaExpiracion) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.familiaId = familiaId;
        this.fechaCreacion = Instant.now();
        this.fechaExpiracion = fechaExpiracion;
    }

    public void revocar(Instant fecha) {
        if (fechaRevocacion == null) {
            fechaRevocacion = fecha;
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public UUID getFamiliaId() {
        return familiaId;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaExpiracion() {
        return fechaExpiracion;
    }

    public Instant getFechaRevocacion() {
        return fechaRevocacion;
    }

    public boolean isRevocado() {
        return fechaRevocacion != null;
    }
}
