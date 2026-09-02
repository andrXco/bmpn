package co.edu.javeriana.bmpn.dto.empresa;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarEmpresaRequest(
        @NotBlank(message = "El NIT es obligatorio")
        @Size(max = 30, message = "El NIT no puede superar 30 caracteres")
        String nit,

        @NotBlank(message = "El nombre de la empresa es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombreEmpresa,

        @NotBlank(message = "El correo de contacto es obligatorio")
        @Email(message = "El correo de contacto no tiene un formato valido")
        @Size(max = 254, message = "El correo de contacto no puede superar 254 caracteres")
        String correoContacto,

        @NotBlank(message = "El nombre del administrador es obligatorio")
        @Size(max = 100, message = "El nombre del administrador no puede superar 100 caracteres")
        String nombreAdministrador,

        @NotBlank(message = "El apellido del administrador es obligatorio")
        @Size(max = 100, message = "El apellido del administrador no puede superar 100 caracteres")
        String apellidoAdministrador,

        @NotBlank(message = "El correo del administrador es obligatorio")
        @Email(message = "El correo del administrador no tiene un formato valido")
        @Size(max = 254, message = "El correo del administrador no puede superar 254 caracteres")
        String emailAdministrador,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
        String password) {
}
