package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

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

import co.edu.javeriana.bmpn.dto.actividad.ActividadResponse;
import co.edu.javeriana.bmpn.dto.actividad.CrearActividadRequest;
import co.edu.javeriana.bmpn.dto.actividad.EditarActividadRequest;
import co.edu.javeriana.bmpn.service.ActividadService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/procesos/{procesoId}/actividades")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @PostMapping
    public ResponseEntity<ActividadResponse> crear(@PathVariable Long procesoId,
                                                   @RequestParam Long usuarioId,
                                                   @Valid @RequestBody CrearActividadRequest request) {
        ActividadResponse actividad = actividadService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/actividades/" + actividad.getId());
        return ResponseEntity.created(ubicacion).body(actividad);
    }

    @GetMapping
    public ResponseEntity<List<ActividadResponse>> listar(@PathVariable Long procesoId,
                                                          @RequestParam Long usuarioId) {
        return ResponseEntity.ok(actividadService.listar(procesoId, usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActividadResponse> obtener(@PathVariable Long procesoId,
                                                     @PathVariable Long id,
                                                     @RequestParam Long usuarioId) {
        return ResponseEntity.ok(actividadService.obtener(procesoId, id, usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActividadResponse> editar(@PathVariable Long procesoId,
                                                    @PathVariable Long id,
                                                    @RequestParam Long usuarioId,
                                                    @Valid @RequestBody EditarActividadRequest request) {
        return ResponseEntity.ok(actividadService.editar(procesoId, id, usuarioId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId,
                                         @PathVariable Long id,
                                         @RequestParam Long usuarioId) {
        actividadService.eliminar(procesoId, id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
