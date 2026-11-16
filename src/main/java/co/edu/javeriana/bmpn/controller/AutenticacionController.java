package co.edu.javeriana.bmpn.controller;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.dto.error.ErrorDto;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.service.AutenticacionService;
import co.edu.javeriana.bmpn.service.RefreshTokenService;
import co.edu.javeriana.bmpn.service.RefreshTokenService.TokenRenovado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "Autenticación", description = "Inicio, renovación y cierre de sesiones.")
@RestController
@RequestMapping("/api/sesiones")
public class AutenticacionController {

    private static final String COOKIE_REFRESH = "bmpn_refresh";

    private final AutenticacionService autenticacionService;
    private final RefreshTokenService refreshTokenService;
    private final Duration expiracionRefresh;
    private final boolean cookieSegura;
    private final String[] origenesPermitidos;

    public AutenticacionController(
            AutenticacionService autenticacionService,
            RefreshTokenService refreshTokenService,
            @Value("${bmpn.security.refresh.expiration}") Duration expiracionRefresh,
            @Value("${bmpn.security.refresh.cookie-secure}") boolean cookieSegura,
            @Value("${app.cors.origenes}") String[] origenesPermitidos) {
        this.autenticacionService = autenticacionService;
        this.refreshTokenService = refreshTokenService;
        this.expiracionRefresh = expiracionRefresh;
        this.cookieSegura = cookieSegura;
        this.origenesPermitidos = origenesPermitidos;
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
                                      "rolAcceso": "ADMINISTRADOR",
                                      "accessToken": "eyJ...",
                                      "tokenType": "Bearer",
                                      "expiresIn": 900
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
        SesionUsuarioResponse sesion = autenticacionService.iniciarSesion(formulario);
        String refreshToken = refreshTokenService.crear(sesion.getUsuarioId());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, crearCookie(refreshToken, expiracionRefresh).toString())
                .body(sesion);
    }

    @PostMapping("/renovacion")
    @Operation(summary = "Renovar sesión", description = "Rota el refresh token HttpOnly y devuelve un nuevo access JWT.")
    public ResponseEntity<SesionUsuarioResponse> renovarSesion(
            @CookieValue(value = COOKIE_REFRESH, required = false) String refreshToken,
            HttpServletRequest request) {
        validarOrigen(request);
        TokenRenovado renovacion = refreshTokenService.renovar(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        crearCookie(renovacion.refreshToken(), expiracionRefresh).toString())
                .body(renovacion.sesion());
    }

    @DeleteMapping
    @Operation(summary = "Cerrar sesión", description = "Revoca la familia del refresh token y elimina la cookie.")
    public ResponseEntity<Void> cerrarSesion(
            @CookieValue(value = COOKIE_REFRESH, required = false) String refreshToken,
            HttpServletRequest request) {
        validarOrigen(request);
        refreshTokenService.revocar(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, crearCookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseCookie crearCookie(String valor, Duration maxAge) {
        return ResponseCookie.from(COOKIE_REFRESH, valor)
                .httpOnly(true)
                .secure(cookieSegura)
                .sameSite("Strict")
                .path("/api/sesiones")
                .maxAge(maxAge)
                .build();
    }

    private void validarOrigen(HttpServletRequest request) {
        String origen = request.getHeader(HttpHeaders.ORIGIN);
        if (origen == null || Arrays.stream(origenesPermitidos).noneMatch(origen::equals)) {
            throw new AccesoDenegadoException("Origen no autorizado para administrar la sesión");
        }
    }
}
