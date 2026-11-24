package co.edu.javeriana.bmpn.entity;

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
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

//Matriz de permisos por pool
//24 (ADMINISTRADOR/EDITOR/SOLO_LECTURA) dentro de ese pool en particular
@Entity
@Table(name = "permiso_pool")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PermisoPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_acceso", nullable = false, length = 30)
    private RolAcceso rolAcceso;

    @Column(name = "puede_crear", nullable = false)
    private boolean puedeCrear;

    @Column(name = "puede_editar", nullable = false)
    private boolean puedeEditar;

    @Column(name = "puede_eliminar", nullable = false)
    private boolean puedeEliminar;

    public PermisoPool(Pool pool, RolAcceso rolAcceso,
                       boolean puedeCrear, boolean puedeEditar, boolean puedeEliminar) {
        this.pool = pool;
        this.rolAcceso = rolAcceso;
        this.puedeCrear = puedeCrear;
        this.puedeEditar = puedeEditar;
        this.puedeEliminar = puedeEliminar;
    }

    public void actualizar(boolean puedeCrear, boolean puedeEditar, boolean puedeEliminar) {
        this.puedeCrear = puedeCrear;
        this.puedeEditar = puedeEditar;
        this.puedeEliminar = puedeEliminar;
    }
}