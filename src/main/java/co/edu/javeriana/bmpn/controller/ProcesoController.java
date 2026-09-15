package co.edu.javeriana.bmpn.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.dto.proceso.CrearProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.EditarProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResponse;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;
import co.edu.javeriana.bmpn.service.ProcesoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;
    private final SesionHttp sesionHttp;

    public ProcesoController(ProcesoService procesoService, SesionHttp sesionHttp) {
        this.procesoService = procesoService;
        this.sesionHttp = sesionHttp;
    }

    @PostMapping
    public ResponseEntity<ProcesoResponse> crear(
            @Valid @RequestBody CrearProcesoRequest formulario,
            HttpServletRequest request) {

        SesionUsuarioResponse sesion = exigirSesion(request);

        ProcesoResponse proceso = procesoService.crear(
                sesion.getEmpresaId(),
                sesion.getUsuarioId(),
                sesion.getRolAcceso(),
                formulario);

        return ResponseEntity
                .created(URI.create("/api/procesos/" + proceso.id()))
                .body(proceso);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcesoResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarProcesoRequest formulario,
            HttpServletRequest request) {

        SesionUsuarioResponse sesion = exigirSesion(request);

        ProcesoResponse proceso = procesoService.editar(
                id,
                sesion.getEmpresaId(),
                sesion.getUsuarioId(),
                sesion.getRolAcceso(),
                formulario);

        return ResponseEntity.ok(proceso);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            HttpServletRequest request) {

        SesionUsuarioResponse sesion = exigirSesion(request);

        procesoService.eliminar(
                id,
                sesion.getEmpresaId(),
                sesion.getUsuarioId(),
                sesion.getRolAcceso());

        return ResponseEntity.noContent().build();
    }

    private SesionUsuarioResponse exigirSesion(HttpServletRequest request) {
        return sesionHttp.obtener(request)
                .orElseThrow(() -> new AutenticacionRequeridaException(
                        "Debe iniciar sesion"));
    }
}