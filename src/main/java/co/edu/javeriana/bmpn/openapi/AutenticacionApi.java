package co.edu.javeriana.bmpn.openapi;

import org.springframework.http.ResponseEntity;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.dto.error.ErrorDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Contrato OpenAPI de los endpoints de autenticación.
 *
 * <p>El controlador implementa este contrato y conserva únicamente la lógica HTTP.</p>
 */
@Tag(name = "Autenticación", description = "Inicio de sesión de usuarios.")
public interface AutenticacionApi {

    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales y devuelve la identidad, empresa y rol del usuario. "
                    + "En esta entrega, el usuarioId devuelto se usa temporalmente en los demás endpoints; "
                    + "todavía no se emite un token de seguridad.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Credenciales del usuario registrado.",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "email": "ana@empresa.com",
                              "password": "ClaveSegura123"
                            }
                            """))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SesionUsuarioResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "usuarioId": 10,
                              "empresaId": 10,
                              "email": "ana@empresa.com",
                              "nombreCompleto": "Ana Prueba",
                              "rolAcceso": "ADMINISTRADOR"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "El cuerpo no cumple las validaciones.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": "SOLICITUD_INVALIDA",
                                      "mensaje": "Uno o mas campos no cumplen las reglas de validacion",
                                      "fecha": "2026-09-26T22:30:00Z",
                                      "detalles": ["email: El correo no tiene un formato valido"]
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Correo o contraseña inválidos.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": "AUTENTICACION_REQUERIDA",
                                      "mensaje": "Correo o contraseña inválidos",
                                      "fecha": "2026-09-26T22:30:00Z",
                                      "detalles": []
                                    }
                                    """)))
    })
    ResponseEntity<SesionUsuarioResponse> iniciarSesion(IniciarSesionRequest formulario);
}
