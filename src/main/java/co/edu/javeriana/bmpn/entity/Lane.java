package co.edu.javeriana.bmpn.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import static lombok.AccessLevel.PROTECTED;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lane")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Lane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_proceso_id", nullable = false)
    private RolProceso rolProceso;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "lane", fetch = FetchType.LAZY)
    private List<ElementoProceso> elementos = new ArrayList<>();

    public Lane(Pool pool, RolProceso rolProceso, int orden) {
        this.pool = pool;
        this.rolProceso = rolProceso;
        this.orden = orden;
        this.activo = true;
    }

    public void cambiarRol(RolProceso rolProceso) {
        this.rolProceso = rolProceso;
    }

    public void desactivar() {
        this.activo = false;
    }

    public boolean tieneElementosActivos() {
        for (ElementoProceso elemento : elementos) {
            if (elemento.isActivo()) {
                return true;
            }
        }
        return false;
    }
}