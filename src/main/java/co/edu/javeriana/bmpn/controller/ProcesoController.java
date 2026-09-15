package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.dto.proceso.CrearProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.EditarProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResponse;
import co.edu.javeriana.bmpn.dto.proceso.HistorialResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResumen;
import co.edu.javeriana.bmpn.entity.EstadoProceso;
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

    @GetMapping
    public ResponseEntity<Page<ProcesoResumen>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoProceso estado,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable,
            HttpServletRequest request) {

        SesionUsuarioResponse sesion = exigirSesion(request);

        return ResponseEntity.ok(procesoService.listar(
                sesion.getEmpresaId(),
                nombre,
                estado,
                categoria,
                incluirInactivos,
                pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcesoResponse> obtenerDetalle(
            @PathVariable Long id,
            HttpServletRequest request) {

        SesionUsuarioResponse sesion = exigirSesion(request);

        return ResponseEntity.ok(
                procesoService.obtenerDetalle(id, sesion.getEmpresaId()));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialResponse>> obtenerHistorial(
            @PathVariable Long id,
            HttpServletRequest request) {

        SesionUsuarioResponse sesion = exigirSesion(request);

        return ResponseEntity.ok(
                procesoService.obtenerHistorial(id, sesion.getEmpresaId()));
    }

    private SesionUsuarioResponse exigirSesion(HttpServletRequest request) {
        return sesionHttp.obtener(request)
                .orElseThrow(() -> new AutenticacionRequeridaException(
                        "Debe iniciar sesion"));
    }
}