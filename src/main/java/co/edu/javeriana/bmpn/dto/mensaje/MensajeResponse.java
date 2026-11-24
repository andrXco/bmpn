package co.edu.javeriana.bmpn.dto.mensaje;

import java.util.ArrayList;
import java.util.List;

import co.edu.javeriana.bmpn.entity.PoliticaFallo;
import co.edu.javeriana.bmpn.entity.PoliticaSinCorrespondencia;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MensajeResponse {

    private Long id;
    private Long procesoId;
    private Long poolOrigenId;
    private Long poolDestinoId;
    private Long eventoEnvioId;
    private Long eventoRecepcionId;
    private String nombre;
    private String claveCorrelacion;
    private PoliticaSinCorrespondencia politicaSinCorrespondencia;
    private PoliticaFallo politicaFallo;
    private boolean notificacionExterna;
    private String canalDestino;
    private boolean activo;
    private List<CampoMensajeResponse> campos = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
}
