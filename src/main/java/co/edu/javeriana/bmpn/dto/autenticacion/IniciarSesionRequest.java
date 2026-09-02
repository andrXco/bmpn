package co.edu.javeriana.bmpn.dto.autenticacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IniciarSesionRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 254, message = "El correo no puede superar 254 caracteres")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(max = 100, message = "La contrasena no puede superar 100 caracteres")
        String password) {
}
