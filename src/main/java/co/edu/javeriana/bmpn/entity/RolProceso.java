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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

//roles de la empresa("Analista", "Aprobador")
//se usa para armar las lanes de los pools y para los permisos por pool
//17 a 24
@Entity
@Table(name = "rol_proceso")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RolProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "rolProceso", fetch = FetchType.LAZY)
    private List<Lane> lanes = new ArrayList<>();

    public RolProceso(Empresa empresa, String nombre, String descripcion) {
        this.empresa = empresa;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public void actualizarDatos(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void desactivar() {
        this.activo = false;
    }

    // Nombres de los procesos que tienen una lane activa con este rol
    public List<String> procesosDondeSeUsa() {
        List<String> procesos = new ArrayList<>();
        for (Lane lane : lanes) {
            String proceso = lane.getPool().getProceso().getNombre();
            if (lane.isActivo() && !procesos.contains(proceso)) {
                procesos.add(proceso);
            }
        }
        return procesos;
    }
}
