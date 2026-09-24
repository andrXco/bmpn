package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.proceso.CrearProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.EditarProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.HistorialResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResumen;
import co.edu.javeriana.bmpn.entity.EstadoProceso;
import co.edu.javeriana.bmpn.service.ProcesoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    @PostMapping
    public ResponseEntity<ProcesoResponse> crear(@RequestParam Long usuarioId,
                                                 @Valid @RequestBody CrearProcesoRequest request) {
        ProcesoResponse proceso = procesoService.crear(usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + proceso.getId());
        return ResponseEntity.created(ubicacion).body(proceso);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcesoResponse> editar(@PathVariable Long id,
                                                  @RequestParam Long usuarioId,
                                                  @Valid @RequestBody EditarProcesoRequest request) {
        return ResponseEntity.ok(procesoService.editar(id, usuarioId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam Long usuarioId) {
        procesoService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProcesoResumen>> listar(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoProceso estado,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest paginacion = PageRequest.of(page, size, Sort.by("nombre"));
        return ResponseEntity.ok(procesoService.listar(usuarioId, nombre, estado,
                categoria, incluirInactivos, paginacion));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcesoResponse> obtenerDetalle(@PathVariable Long id,
                                                          @RequestParam Long usuarioId) {
        return ResponseEntity.ok(procesoService.obtenerDetalle(id, usuarioId));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialResponse>> obtenerHistorial(@PathVariable Long id,
                                                                    @RequestParam Long usuarioId) {
        return ResponseEntity.ok(procesoService.obtenerHistorial(id, usuarioId));
    }
}