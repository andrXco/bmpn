package co.edu.javeriana.bmpn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

//24 Roles del catalogo de la empresa
@Entity
@Table(name = "pool_rol_disponible")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PoolRolDisponible {

    @EmbeddedId
    private PoolRolDisponibleId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("poolId")
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("rolProcesoId")
    @JoinColumn(name = "rol_proceso_id", nullable = false)
    private RolProceso rolProceso;

    @Column(nullable = false)
    private boolean activo;

    public PoolRolDisponible(Pool pool, RolProceso rolProceso) {
        this.pool = pool;
        this.rolProceso = rolProceso;
        this.id = new PoolRolDisponibleId(pool.getId(), rolProceso.getId());
        this.activo = true;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }
}