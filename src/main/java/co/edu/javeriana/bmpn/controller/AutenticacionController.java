package co.edu.javeriana.bmpn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.service.AutenticacionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sesiones")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping
    public ResponseEntity<SesionUsuarioResponse> iniciarSesion(
            @Valid @RequestBody IniciarSesionRequest formulario) {
        return ResponseEntity.ok(autenticacionService.iniciarSesion(formulario));
    }
}
