package co.edu.javeriana.bmpn.dto.usuario;

import co.edu.javeriana.bmpn.entity.RolAcceso;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarUsuarioRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 254, message = "El correo no puede superar 254 caracteres")
        String email,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String apellido,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
        String password,

        @NotNull(message = "El rol de acceso es obligatorio")
        RolAcceso rolAcceso) {
}
