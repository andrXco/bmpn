package co.edu.javeriana.bmpn.dto.pool;

import co.edu.javeriana.bmpn.entity.TipoParticipante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CrearPoolRequest {

    @NotBlank(message = "El nombre del pool es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de participante es obligatorio")
    private TipoParticipante tipoParticipante;

    //obligatorio para CLIENTE/PROVEEDOR
    private Long empresaParticipanteId;

    @Size(max = 100, message = "El canal externo no puede superar 100 caracteres")
    private String canalExterno;

    private boolean cajaNegra;
}
