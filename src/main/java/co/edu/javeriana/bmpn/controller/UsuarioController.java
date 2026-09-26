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

import co.edu.javeriana.bmpn.dto.usuario.CambiarRolUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.RegistrarUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(usuarioService.listarActivos(usuarioId));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(
            @RequestParam Long usuarioId,
            @Valid @RequestBody RegistrarUsuarioRequest formulario) {
        UsuarioResponse usuario = usuarioService.registrar(usuarioId, formulario);
        return ResponseEntity
                .created(URI.create("/api/usuarios/" + usuario.getId()))
                .body(usuario);
    }

    @PatchMapping("/{usuarioId}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @PathVariable("usuarioId") Long usuarioObjetivoId,
            @RequestParam Long usuarioId,
            @Valid @RequestBody CambiarRolUsuarioRequest formulario) {

        UsuarioResponse usuario = usuarioService.cambiarRol(
                usuarioId,
                usuarioObjetivoId,
                formulario);

        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> desactivar(
            @PathVariable("usuarioId") Long usuarioObjetivoId,
            @RequestParam Long usuarioId) {
        usuarioService.desactivar(usuarioId, usuarioObjetivoId);
        return ResponseEntity.noContent().build();
    }
}
