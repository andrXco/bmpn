package co.edu.javeriana.bmpn.dto.proceso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CrearProcesoRequest(

        @NotBlank(message = "El nombre del proceso es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @NotBlank(message = "La descripcion es obligatoria")
        String descripcion,

        @NotBlank(message = "La categoria es obligatoria")
        @Size(max = 100, message = "La categoria no puede superar 100 caracteres")
        String categoria) {
}