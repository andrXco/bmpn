package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import co.edu.javeriana.bmpn.dto.usuario.CambiarRolUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.RegistrarUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Administración de colaboradores de una empresa.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Listar colaboradores activos",
            description = "Devuelve los usuarios activos de la empresa del administrador solicitante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colaboradores consultados correctamente."),
            @ApiResponse(responseCode = "400", description = "Falta o es inválido el usuarioId."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos administrativos.")
    })
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(
            @Parameter(description = "Identificador del administrador que realiza la consulta.", example = "10")
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(usuarioService.listarActivos(usuarioId));
    }

    @Operation(
            summary = "Registrar colaborador",
            description = "Registra un colaborador en la empresa del administrador solicitante.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Colaborador registrado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos administrativos."),
            @ApiResponse(responseCode = "409", description = "El correo ya está registrado en la empresa.")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(
            @Parameter(description = "Identificador del administrador que registra al colaborador.", example = "10")
            @RequestParam Long usuarioId,
            @Valid @RequestBody RegistrarUsuarioRequest formulario) {
        UsuarioResponse usuario = usuarioService.registrar(usuarioId, formulario);
        return ResponseEntity
                .created(URI.create("/api/usuarios/" + usuario.getId()))
                .body(usuario);
    }

    @Operation(
            summary = "Cambiar rol de colaborador",
            description = "Actualiza el rol de un colaborador de la misma empresa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos administrativos."),
            @ApiResponse(responseCode = "404", description = "El colaborador objetivo no existe.")
    })
    @PatchMapping("/{usuarioId}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @Parameter(description = "Identificador del colaborador cuyo rol cambiará.", example = "11")
            @PathVariable("usuarioId") Long usuarioObjetivoId,
            @Parameter(description = "Identificador del administrador que realiza el cambio.", example = "10")
            @RequestParam Long usuarioId,
            @Valid @RequestBody CambiarRolUsuarioRequest formulario) {

        UsuarioResponse usuario = usuarioService.cambiarRol(
                usuarioId,
                usuarioObjetivoId,
                formulario);

        return ResponseEntity.ok(usuario);
    }

    @Operation(
            summary = "Desactivar colaborador",
            description = "Realiza el borrado lógico de un colaborador de la misma empresa.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Colaborador desactivado correctamente."),
            @ApiResponse(responseCode = "400", description = "Falta o es inválido algún identificador."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos administrativos."),
            @ApiResponse(responseCode = "404", description = "El colaborador objetivo no existe.")
    })
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "Identificador del colaborador que se desactivará.", example = "11")
            @PathVariable("usuarioId") Long usuarioObjetivoId,
            @Parameter(description = "Identificador del administrador que realiza la desactivación.", example = "10")
            @RequestParam Long usuarioId) {
        usuarioService.desactivar(usuarioId, usuarioObjetivoId);
        return ResponseEntity.noContent().build();
    }
}
