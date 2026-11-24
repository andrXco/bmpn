package co.edu.javeriana.bmpn.dto.autenticacion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public class IniciarSesionRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 254, message = "El correo no puede superar 254 caracteres")
    @Schema(description = "Correo del usuario registrado.", example = "ana@empresa.com")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(max = 100, message = "La contrasena no puede superar 100 caracteres")
    @Schema(description = "Contraseña del usuario.", example = "ClaveSegura123")
    private String password;

    public IniciarSesionRequest() {
    }

    public IniciarSesionRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
