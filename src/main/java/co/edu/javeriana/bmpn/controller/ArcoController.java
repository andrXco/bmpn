package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.arco.ArcoResponse;
import co.edu.javeriana.bmpn.dto.arco.CrearArcoRequest;
import co.edu.javeriana.bmpn.service.ArcoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/procesos/{procesoId}/arcos")
public class ArcoController {

    private final ArcoService arcoService;

    public ArcoController(ArcoService arcoService) {
        this.arcoService = arcoService;
    }

    @PostMapping
    public ResponseEntity<ArcoResponse> crear(@PathVariable Long procesoId,
                                              @RequestParam Long usuarioId,
                                              @Valid @RequestBody CrearArcoRequest request) {
        ArcoResponse arco = arcoService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/arcos/" + arco.getId());
        return ResponseEntity.created(ubicacion).body(arco);
    }

    @GetMapping
    public ResponseEntity<List<ArcoResponse>> listar(@PathVariable Long procesoId,
                                                     @RequestParam Long usuarioId) {
        return ResponseEntity.ok(arcoService.listar(procesoId, usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArcoResponse> obtener(@PathVariable Long procesoId,
                                                @PathVariable Long id,
                                                @RequestParam Long usuarioId) {
        return ResponseEntity.ok(arcoService.obtener(procesoId, id, usuarioId));
    }
}