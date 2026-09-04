package co.edu.javeriana.bmpn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;
import co.edu.javeriana.bmpn.service.AutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sesiones")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;
    private final SesionHttp sesionHttp;

    public AutenticacionController(
            AutenticacionService autenticacionService,
            SesionHttp sesionHttp) {
        this.autenticacionService = autenticacionService;
        this.sesionHttp = sesionHttp;
    }

    @GetMapping
    public SesionUsuarioResponse consultarSesion(HttpServletRequest request) {
        return sesionHttp.obtener(request)
                .orElseThrow(() -> new AutenticacionRequeridaException(
                        "Debe iniciar sesion"));
    }

    @PostMapping
    public SesionUsuarioResponse iniciarSesion(
            @Valid @RequestBody IniciarSesionRequest formulario,
            HttpServletRequest request) {
        SesionUsuarioResponse sesion = autenticacionService.iniciarSesion(formulario);
        sesionHttp.guardar(request, sesion);
        return sesion;
    }

    @DeleteMapping
    public ResponseEntity<Void> cerrarSesion(HttpServletRequest request) {
        sesionHttp.cerrar(request);
        return ResponseEntity.noContent().build();
    }
}
