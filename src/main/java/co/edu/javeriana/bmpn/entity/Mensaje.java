package co.edu.javeriana.bmpn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "mensaje")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pool_origen_id", nullable = false)
    private Pool poolOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pool_destino_id", nullable = false)
    private Pool poolDestino;

    @Column(nullable = false, length = 150)
    private String nombre;

    //28: llave para relacionar este mensaje con otros del mismo proceso
    @Column(name = "clave_correlacion", nullable = false, length = 150)
    private String claveCorrelacion;

    @Column(name = "politica_sin_correspondencia", length = 500)
    private String politicaSinCorrespondencia;

    @Column(name = "politica_fallo", length = 500)
    private String politicaFallo;

    @Column(nullable = false)
    private boolean activo;

    public Mensaje(Proceso proceso, Pool poolOrigen, Pool poolDestino, String nombre,
                   String claveCorrelacion, String politicaSinCorrespondencia, String politicaFallo) {
        this.proceso = proceso;
        this.poolOrigen = poolOrigen;
        this.poolDestino = poolDestino;
        this.nombre = nombre;
        this.claveCorrelacion = claveCorrelacion;
        this.politicaSinCorrespondencia = politicaSinCorrespondencia;
        this.politicaFallo = politicaFallo;
        this.activo = true;
    }

    //26 si el destino es un pool de sistema externo, este mensaje es una notificacion externa
    public boolean esNotificacionExterna() {
        return poolDestino.getTipoParticipante() == TipoParticipante.SISTEMA_EXTERNO;
    }

    public void desactivar() {
        this.activo = false;
    }
}