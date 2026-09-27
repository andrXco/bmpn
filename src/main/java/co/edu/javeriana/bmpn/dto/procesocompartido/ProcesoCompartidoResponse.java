package co.edu.javeriana.bmpn.dto.procesocompartido;

import java.time.Instant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcesoCompartidoResponse {
    private Long id;
    private Long procesoId;
    private Long empresaInvitadaId;
    private String nombreEmpresaInvitada;
    private Instant fechaCreacion;
    private boolean activo;
}
