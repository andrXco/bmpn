package co.edu.javeriana.bmpn.dto.mensaje;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CampoMensajeResponse {

    private String nombre;
    private String tipoDato;
    private String descripcion;
    private boolean obligatorio;
}
