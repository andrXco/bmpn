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
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.dto.usuario.CambiarRolUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.RegistrarUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;
import co.edu.javeriana.bmpn.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SesionHttp sesionHttp;

    public UsuarioController(UsuarioService usuarioService, SesionHttp sesionHttp) {
        this.usuarioService = usuarioService;
        this.sesionHttp = sesionHttp;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(HttpServletRequest request) {
        SesionUsuarioResponse sesion = exigirSesion(request);
        List<UsuarioResponse> usuarios = usuarioService.listarActivos(sesion.getEmpresaId());
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistrarUsuarioRequest formulario,
            HttpServletRequest request) {
        SesionUsuarioResponse sesion = exigirSesion(request);
        UsuarioResponse usuario = usuarioService.registrar(
                sesion.getEmpresaId(), sesion.getRolAcceso(), formulario);
        return ResponseEntity
                .created(URI.create("/api/usuarios/" + usuario.getId()))
                .body(usuario);
    }

    @PatchMapping("/{usuarioId}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @PathVariable Long usuarioId,
            @Valid @RequestBody CambiarRolUsuarioRequest formulario,
            HttpServletRequest request) {
        SesionUsuarioResponse sesion = exigirSesion(request);
        UsuarioResponse usuario = usuarioService.cambiarRol(
                sesion.getEmpresaId(), sesion.getRolAcceso(), usuarioId, formulario);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> desactivar(
            @PathVariable Long usuarioId,
            HttpServletRequest request) {
        SesionUsuarioResponse sesion = exigirSesion(request);
        usuarioService.desactivar(sesion.getEmpresaId(), sesion.getRolAcceso(), usuarioId);
        return ResponseEntity.noContent().build();
    }

    private SesionUsuarioResponse exigirSesion(HttpServletRequest request) {
        return sesionHttp.obtener(request)
                .orElseThrow(() -> new AutenticacionRequeridaException(
                        "Debe iniciar sesion"));
    }
}
