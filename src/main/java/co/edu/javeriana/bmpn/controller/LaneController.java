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

import co.edu.javeriana.bmpn.dto.lane.CrearLaneRequest;
import co.edu.javeriana.bmpn.dto.lane.EditarLaneRequest;
import co.edu.javeriana.bmpn.dto.lane.LaneResponse;
import co.edu.javeriana.bmpn.service.LaneService;
import jakarta.validation.Valid;

// Lanes de un pool
//22
@RestController
@RequestMapping("/api/procesos/{procesoId}/pools/{poolId}/lanes")
public class LaneController {

    private final LaneService laneService;

    public LaneController(LaneService laneService) {
        this.laneService = laneService;
    }

    @PostMapping
    public ResponseEntity<LaneResponse> crear(@PathVariable Long procesoId,
                                              @PathVariable Long poolId,
                                              @RequestParam Long usuarioId,
                                              @Valid @RequestBody CrearLaneRequest request) {
        LaneResponse lane = laneService.crear(procesoId, poolId, usuarioId, request);
        URI ubicacion = URI.create(
                "/api/procesos/" + procesoId + "/pools/" + poolId + "/lanes/" + lane.getId());
        return ResponseEntity.created(ubicacion).body(lane);
    }

    @PutMapping("/{laneId}")
    public ResponseEntity<LaneResponse> editar(@PathVariable Long procesoId,
                                               @PathVariable Long poolId,
                                               @PathVariable Long laneId,
                                               @RequestParam Long usuarioId,
                                               @Valid @RequestBody EditarLaneRequest request) {
        return ResponseEntity.ok(laneService.editar(procesoId, poolId, laneId, usuarioId, request));
    }

    @DeleteMapping("/{laneId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId,
                                         @PathVariable Long poolId,
                                         @PathVariable Long laneId,
                                         @RequestParam Long usuarioId) {
        laneService.eliminar(procesoId, poolId, laneId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<LaneResponse>> listar(@PathVariable Long procesoId,
                                                      @PathVariable Long poolId,
                                                      @RequestParam Long usuarioId) {
        return ResponseEntity.ok(laneService.listar(procesoId, poolId, usuarioId));
    }
}
