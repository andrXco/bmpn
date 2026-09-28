package co.edu.javeriana.bmpn.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import static lombok.AccessLevel.PROTECTED;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class PoolRolDisponibleId implements Serializable {

    @Column(name = "pool_id")
    private Long poolId;

    @Column(name = "rol_proceso_id")
    private Long rolProcesoId;
}
