package co.edu.javeriana.bmpn.dto.proceso;

import co.edu.javeriana.bmpn.entity.EstadoProceso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditarProcesoRequest {

    @NotBlank(message = "El nombre del proceso es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    @Schema(description = "Nombre del proceso BPMN.", example = "Proceso de compras")
    private String nombre;

    @NotBlank(message = "La descripcion es obligatoria")
    @Schema(description = "Descripción funcional del proceso.", example = "Gestiona las solicitudes de compra.")
    private String descripcion;

    @NotBlank(message = "La categoria es obligatoria")
    @Size(max = 100, message = "La categoria no puede superar 100 caracteres")
    @Schema(description = "Categoría del proceso.", example = "Abastecimiento")
    private String categoria;

    @NotNull(message = "El estado es obligatorio")
    @Schema(description = "Estado del proceso.", example = "PUBLICADO")
    private EstadoProceso estado;
}
