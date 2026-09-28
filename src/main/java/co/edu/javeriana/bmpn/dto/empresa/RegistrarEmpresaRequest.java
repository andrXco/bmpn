package co.edu.javeriana.bmpn.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrarEmpresaRequest {

    @NotBlank(message = "El NIT es obligatorio")
    @Size(max = 30, message = "El NIT no puede superar 30 caracteres")
    @Schema(description = "NIT de la empresa.", example = "900123456-7")
    private String nit;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    @Schema(description = "Nombre legal o comercial de la empresa.", example = "Empresa de Prueba S.A.S.")
    private String nombreEmpresa;

    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "El correo de contacto no tiene un formato valido")
    @Size(max = 254, message = "El correo de contacto no puede superar 254 caracteres")
    @Schema(description = "Correo de contacto de la empresa.", example = "contacto@empresa.com")
    private String correoContacto;

    @NotBlank(message = "El nombre del administrador es obligatorio")
    @Size(max = 100, message = "El nombre del administrador no puede superar 100 caracteres")
    @Schema(description = "Nombre del administrador inicial.", example = "Ana")
    private String nombreAdministrador;

    @NotBlank(message = "El apellido del administrador es obligatorio")
    @Size(max = 100, message = "El apellido del administrador no puede superar 100 caracteres")
    @Schema(description = "Apellido del administrador inicial.", example = "Prueba")
    private String apellidoAdministrador;

    @NotBlank(message = "El correo del administrador es obligatorio")
    @Email(message = "El correo del administrador no tiene un formato valido")
    @Size(max = 254, message = "El correo del administrador no puede superar 254 caracteres")
    @Schema(description = "Correo con el que el administrador iniciará sesión.", example = "ana@empresa.com")
    private String emailAdministrador;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    @Schema(description = "Contraseña inicial del administrador.", example = "ClaveSegura123")
    private String password;

    public RegistrarEmpresaRequest() {
    }

    public RegistrarEmpresaRequest(
            String nit,
            String nombreEmpresa,
            String correoContacto,
            String nombreAdministrador,
            String apellidoAdministrador,
            String emailAdministrador,
            String password) {
        this.nit = nit;
        this.nombreEmpresa = nombreEmpresa;
        this.correoContacto = correoContacto;
        this.nombreAdministrador = nombreAdministrador;
        this.apellidoAdministrador = apellidoAdministrador;
        this.emailAdministrador = emailAdministrador;
        this.password = password;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    public String getNombreAdministrador() {
        return nombreAdministrador;
    }

    public void setNombreAdministrador(String nombreAdministrador) {
        this.nombreAdministrador = nombreAdministrador;
    }

    public String getApellidoAdministrador() {
        return apellidoAdministrador;
    }

    public void setApellidoAdministrador(String apellidoAdministrador) {
        this.apellidoAdministrador = apellidoAdministrador;
    }

    public String getEmailAdministrador() {
        return emailAdministrador;
    }

    public void setEmailAdministrador(String emailAdministrador) {
        this.emailAdministrador = emailAdministrador;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
