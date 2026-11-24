package co.edu.javeriana.bmpn.dto.usuario;

import co.edu.javeriana.bmpn.entity.RolAcceso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegistrarUsuarioRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 254, message = "El correo no puede superar 254 caracteres")
    @Schema(description = "Correo del colaborador.", example = "carlos@empresa.com")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Schema(description = "Nombre del colaborador.", example = "Carlos")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    @Schema(description = "Apellido del colaborador.", example = "Pérez")
    private String apellido;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    @Schema(description = "Contraseña inicial del colaborador.", example = "ClaveSegura123")
    private String password;

    @NotNull(message = "El rol de acceso es obligatorio")
    @Schema(description = "Rol asignado al colaborador.", example = "EDITOR")
    private RolAcceso rolAcceso;

    public RegistrarUsuarioRequest() {
    }

    public RegistrarUsuarioRequest(
            String email,
            String nombre,
            String apellido,
            String password,
            RolAcceso rolAcceso) {
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.password = password;
        this.rolAcceso = rolAcceso;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolAcceso getRolAcceso() {
        return rolAcceso;
    }

    public void setRolAcceso(RolAcceso rolAcceso) {
        this.rolAcceso = rolAcceso;
    }
}
