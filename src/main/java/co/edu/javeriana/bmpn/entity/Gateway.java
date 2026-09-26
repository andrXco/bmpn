package co.edu.javeriana.bmpn.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "gateway")
@PrimaryKeyJoinColumn(name = "elemento_id")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Gateway extends ElementoProceso {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_gateway", nullable = false, length = 20)
    private TipoGateway tipoGateway;

    public Gateway(Proceso proceso, Pool pool, String nombre, TipoGateway tipoGateway,
                   BigDecimal posicionX, BigDecimal posicionY) {
        super(proceso, pool, nombre, posicionX, posicionY);
        this.tipoGateway = tipoGateway;
    }

    public void cambiarTipo(TipoGateway tipoGateway) {
        this.tipoGateway = tipoGateway;
    }

    // El paralelo toma todos los caminos a la vez, por eso sus salidas no llevan condicion
    @Override
    public boolean aceptaCondiciones() {
        return tipoGateway != TipoGateway.PARALELO;
    }
}