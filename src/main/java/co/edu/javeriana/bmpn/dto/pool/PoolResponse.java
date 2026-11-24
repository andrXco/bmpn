package co.edu.javeriana.bmpn.dto.pool;

import co.edu.javeriana.bmpn.entity.TipoParticipante;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PoolResponse {
    private Long id;
    private Long procesoId;
    private Long empresaParticipanteId;
    private String nombre;
    private TipoParticipante tipoParticipante;
    private String canalExterno;
    private boolean cajaNegra;
    private int orden;
    private boolean activo;
}
