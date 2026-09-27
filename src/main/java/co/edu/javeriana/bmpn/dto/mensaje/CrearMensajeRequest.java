package co.edu.javeriana.bmpn.dto.mensaje;

import java.util.ArrayList;
import java.util.List;

import co.edu.javeriana.bmpn.entity.PoliticaFallo;
import co.edu.javeriana.bmpn.entity.PoliticaSinCorrespondencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CrearMensajeRequest {

    @NotNull(message = "El evento que envia el mensaje es obligatorio")
    private Long eventoEnvioId;

    // Evento que recibe el mensaje; si no se envia, se indica el pool destino (sistema externo)
    private Long eventoRecepcionId;

    private Long poolDestinoId;

    @NotBlank(message = "El nombre del mensaje es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @NotBlank(message = "La clave de correlacion es obligatoria")
    @Size(max = 150, message = "La clave de correlacion no puede superar 150 caracteres")
    private String claveCorrelacion;

    private PoliticaSinCorrespondencia politicaSinCorrespondencia;

    private PoliticaFallo politicaFallo;

    @Valid
    private List<CampoMensajeRequest> campos = new ArrayList<>();
}
