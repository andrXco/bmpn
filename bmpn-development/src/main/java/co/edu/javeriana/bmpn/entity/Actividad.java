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
@Table(name = "actividad")
@PrimaryKeyJoinColumn(name = "elemento_id")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Actividad extends ElementoProceso {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actividad", nullable = false, length = 50)
    private TipoActividad tipoActividad;

    public Actividad(Proceso proceso, Pool pool, String nombre, TipoActividad tipoActividad,
                     BigDecimal posicionX, BigDecimal posicionY) {
        super(proceso, pool, nombre, posicionX, posicionY);
        this.tipoActividad = tipoActividad;
    }

    public void cambiarTipo(TipoActividad tipoActividad) {
        this.tipoActividad = tipoActividad;
    }
}
