package co.edu.javeriana.bmpn.entity;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "pool")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_participante_id")
    private Empresa empresaParticipante;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_participante", nullable = false, length = 50)
    private TipoParticipante tipoParticipante;

    @Column(name = "canal_externo", length = 100)
    private String canalExterno;

    @Column(name = "caja_negra", nullable = false)
    private boolean cajaNegra;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "pool", fetch = FetchType.LAZY)
    private List<Lane> lanes = new ArrayList<>();

    @OneToMany(mappedBy = "pool", fetch = FetchType.LAZY)
    private List<ElementoProceso> elementos = new ArrayList<>();

    @OneToMany(mappedBy = "poolOrigen", fetch = FetchType.LAZY)
    private List<Mensaje> mensajesEnviados = new ArrayList<>();

    @OneToMany(mappedBy = "poolDestino", fetch = FetchType.LAZY)
    private List<Mensaje> mensajesRecibidos = new ArrayList<>();

    public Pool(
            Empresa empresaParticipante,
            String nombre,
            TipoParticipante tipoParticipante,
            int orden) {
        this(empresaParticipante, nombre, tipoParticipante, orden, null, false);
    }

    public Pool(
            Empresa empresaParticipante,
            String nombre,
            TipoParticipante tipoParticipante,
            int orden,
            String canalExterno,
            boolean cajaNegra) {
        this.empresaParticipante = empresaParticipante;
        this.nombre = nombre;
        this.tipoParticipante = tipoParticipante;
        this.orden = orden;
        this.canalExterno = canalExterno;
        this.cajaNegra = cajaNegra;
        this.activo = true;
    }

    public void asignarProceso(Proceso proceso) {
        this.proceso = proceso;
    }

    public boolean esPropietario() {
        return tipoParticipante == TipoParticipante.EMPRESA_PROPIETARIA;
    }

    public void renombrar(String nombre) {
        this.nombre = nombre;
    }

    public void actualizarConfiguracion(String canalExterno, boolean cajaNegra) {
        this.canalExterno = canalExterno;
        this.cajaNegra = cajaNegra;
    }

    // La eliminacion es logica: lo que esta dentro del pool se desactiva con lo que tiene adentro
    public void desactivar() {
        this.activo = false;
        for (Lane lane : lanes) {
            lane.desactivar();
        }
        for (ElementoProceso elemento : elementos) {
            elemento.desactivar();
        }
        for (Mensaje mensaje : mensajesEnviados) {
            mensaje.desactivar();
        }
        for (Mensaje mensaje : mensajesRecibidos) {
            mensaje.desactivar();
        }
    }
}
