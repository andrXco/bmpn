package co.edu.javeriana.bmpn.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

// HU-25 a HU-28: comunicacion entre pools, de un evento que envia a uno que recibe
@Entity
@Table(name = "mensaje")
@NamedQuery(
        name = "Mensaje.listarActivosPorProceso",
        query = "SELECT m FROM Mensaje m WHERE m.proceso.id = :procesoId AND m.activo = true ORDER BY m.id")
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

    // Message Throw
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_origen_id")
    private Evento eventoEnvio;

    // Message Catch; queda vacio si el destino es un sistema externo de caja negra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_destino_id")
    private Evento eventoRecepcion;

    @Column(nullable = false, length = 150)
    private String nombre;

    // HU-28: dato de negocio que dice a que caso del proceso corresponde el mensaje
    @Column(name = "clave_correlacion", nullable = false, length = 150)
    private String claveCorrelacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "politica_sin_correspondencia", length = 500)
    private PoliticaSinCorrespondencia politicaSinCorrespondencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "politica_fallo", length = 500)
    private PoliticaFallo politicaFallo;

    @Column(nullable = false)
    private boolean activo;

    // Los campos no existen sin su mensaje, por eso se guardan junto con el
    @OneToMany(mappedBy = "mensaje", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<CampoMensaje> campos = new ArrayList<>();

    public Mensaje(Proceso proceso, Evento eventoEnvio, Evento eventoRecepcion, Pool poolDestino,
                   String nombre, String claveCorrelacion,
                   PoliticaSinCorrespondencia politicaSinCorrespondencia, PoliticaFallo politicaFallo) {
        this.proceso = proceso;
        this.eventoEnvio = eventoEnvio;
        this.eventoRecepcion = eventoRecepcion;
        this.poolOrigen = eventoEnvio.getPool();
        this.poolDestino = poolDestino;
        this.nombre = nombre;
        this.claveCorrelacion = claveCorrelacion;
        this.politicaSinCorrespondencia = politicaSinCorrespondencia;
        this.politicaFallo = politicaFallo;
        this.activo = true;
    }

    public void agregarCampo(CampoMensaje campo) {
        campos.add(campo);
        campo.asignarMensaje(this);
    }

    // HU-26: si el destino es un sistema externo, el mensaje es una notificacion externa
    public boolean esNotificacionExterna() {
        return poolDestino.getTipoParticipante() == TipoParticipante.SISTEMA_EXTERNO;
    }

    public void desactivar() {
        this.activo = false;
    }
}
