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

    public Pool(
            Empresa empresaParticipante,
            String nombre,
            TipoParticipante tipoParticipante,
            int orden) {
        this.empresaParticipante = empresaParticipante;
        this.nombre = nombre;
        this.tipoParticipante = tipoParticipante;
        this.orden = orden;
        this.cajaNegra = false;
        this.activo = true;
    }

    void asignarProceso(Proceso proceso) {
        this.proceso = proceso;
    }

    public boolean esPropietario() {
        return tipoParticipante == TipoParticipante.EMPRESA_PROPIETARIA;
    }

    public void renombrar(String nombre) {
        this.nombre = nombre;
    }

    public void desactivar() {
        this.activo = false;
    }
}