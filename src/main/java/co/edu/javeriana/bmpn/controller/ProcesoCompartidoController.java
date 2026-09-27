package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.procesocompartido.CompartirProcesoRequest;
import co.edu.javeriana.bmpn.dto.procesocompartido.ProcesoCompartidoResponse;
import co.edu.javeriana.bmpn.service.ProcesoCompartidoService;
import jakarta.validation.Valid;

//Colaboracion entre empresas sobre un proceso 
//23
@RestController
@RequestMapping("/api/procesos/{procesoId}/compartidos")
public class ProcesoCompartidoController {

    private final ProcesoCompartidoService procesoCompartidoService;

    public ProcesoCompartidoController(ProcesoCompartidoService procesoCompartidoService) {
        this.procesoCompartidoService = procesoCompartidoService;
    }

    @PostMapping
    public ResponseEntity<ProcesoCompartidoResponse> compartir(@PathVariable Long procesoId,
                                                                @RequestParam Long usuarioId,
                                                                @Valid @RequestBody CompartirProcesoRequest request) {
        ProcesoCompartidoResponse compartido = procesoCompartidoService.compartir(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/compartidos/" + compartido.getId());
        return ResponseEntity.created(ubicacion).body(compartido);
    }

    @DeleteMapping("/{compartidoId}")
    public ResponseEntity<Void> revocar(@PathVariable Long procesoId,
                                        @PathVariable Long compartidoId,
                                        @RequestParam Long usuarioId) {
        procesoCompartidoService.revocar(procesoId, compartidoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ProcesoCompartidoResponse>> listar(@PathVariable Long procesoId,
                                                                   @RequestParam Long usuarioId) {
        return ResponseEntity.ok(procesoCompartidoService.listar(procesoId, usuarioId));
    }
}
