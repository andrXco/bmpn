package co.edu.javeriana.bmpn.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "evento")
@NamedQuery(
        name = "Evento.listarActivosPorProceso",
        query = "SELECT e FROM Evento e WHERE e.proceso.id = :procesoId AND e.activo = true ORDER BY e.id")
@PrimaryKeyJoinColumn(name = "elemento_id")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Evento extends ElementoProceso {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 50)
    private TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DisparadorEvento disparador;

    // Un Message Catch cuyo mensaje llega desde fuera del diagrama
    @Column(name = "origen_externo", nullable = false)
    private boolean origenExterno;

    @OneToMany(mappedBy = "eventoEnvio", fetch = FetchType.LAZY)
    private List<Mensaje> mensajesEnviados = new ArrayList<>();

    @OneToMany(mappedBy = "eventoRecepcion", fetch = FetchType.LAZY)
    private List<Mensaje> mensajesRecibidos = new ArrayList<>();

    public Evento(Proceso proceso, Pool pool, String nombre, TipoEvento tipoEvento,
                  DisparadorEvento disparador, boolean origenExterno,
                  BigDecimal posicionX, BigDecimal posicionY) {
        super(proceso, pool, nombre, posicionX, posicionY);
        this.tipoEvento = tipoEvento;
        this.disparador = disparador;
        this.origenExterno = origenExterno;
    }

    public void actualizar(TipoEvento tipoEvento, DisparadorEvento disparador, boolean origenExterno) {
        this.tipoEvento = tipoEvento;
        this.disparador = disparador;
        this.origenExterno = origenExterno;
    }

    public boolean esEnvioDeMensaje() {
        return disparador == DisparadorEvento.MENSAJE_ENVIO;
    }

    public boolean esRecepcionDeMensaje() {
        return disparador == DisparadorEvento.MENSAJE_RECEPCION;
    }

    public boolean enviaAlgunMensaje() {
        for (Mensaje mensaje : mensajesEnviados) {
            if (mensaje.isActivo()) {
                return true;
            }
        }
        return false;
    }

    public boolean recibeAlgunMensaje() {
        for (Mensaje mensaje : mensajesRecibidos) {
            if (mensaje.isActivo()) {
                return true;
            }
        }
        return false;
    }

    // Un evento de inicio arranca el proceso: nada llega antes que el
    @Override
    public boolean aceptaArcosEntrantes() {
        return tipoEvento != TipoEvento.INICIO;
    }

    // Un evento de fin termina el proceso: nada sale despues de el
    @Override
    public boolean aceptaArcosSalientes() {
        return tipoEvento != TipoEvento.FIN;
    }
}
