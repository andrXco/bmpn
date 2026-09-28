package co.edu.javeriana.bmpn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.dto.error.ErrorDto;
import co.edu.javeriana.bmpn.service.AutenticacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Autenticación", description = "Inicio de sesión de usuarios.")
@RestController
@RequestMapping("/api/sesiones")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales y devuelve la identidad, empresa y rol del usuario.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Credenciales de un usuario registrado.",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "email": "ana@empresa.com",
                              "password": "ClaveSegura123"
                            }
                            """))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SesionUsuarioResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "usuarioId": 10,
                                      "empresaId": 10,
                                      "email": "ana@empresa.com",
                                      "nombreCompleto": "Ana Prueba",
                                      "rolAcceso": "ADMINISTRADOR"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Correo o contraseña inválidos.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping
    public ResponseEntity<SesionUsuarioResponse> iniciarSesion(
            @Valid @RequestBody IniciarSesionRequest formulario) {
        return ResponseEntity.ok(autenticacionService.iniciarSesion(formulario));
    }
}
