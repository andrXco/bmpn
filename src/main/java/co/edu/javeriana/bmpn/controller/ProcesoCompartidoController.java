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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

//Colaboracion entre empresas sobre un proceso 
//23
@Tag(name = "Procesos compartidos", description = "Colaboración entre empresas sobre un proceso BPMN.")
@RestController
@RequestMapping("/api/procesos/{procesoId}/compartidos")
public class ProcesoCompartidoController {

    private final ProcesoCompartidoService procesoCompartidoService;

    public ProcesoCompartidoController(ProcesoCompartidoService procesoCompartidoService) {
        this.procesoCompartidoService = procesoCompartidoService;
    }

    @Operation(summary = "Compartir proceso", description = "Otorga colaboración sobre un proceso a otra empresa.")
    @ApiResponse(responseCode = "201", description = "Proceso compartido correctamente.")
    @PostMapping
    public ResponseEntity<ProcesoCompartidoResponse> compartir(@PathVariable Long procesoId,
                                                                @RequestParam Long usuarioId,
                                                                @Valid @RequestBody CompartirProcesoRequest request) {
        ProcesoCompartidoResponse compartido = procesoCompartidoService.compartir(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/compartidos/" + compartido.getId());
        return ResponseEntity.created(ubicacion).body(compartido);
    }

    @Operation(summary = "Revocar proceso compartido", description = "Retira una colaboración previamente otorgada.")
    @ApiResponse(responseCode = "204", description = "Colaboración revocada correctamente.")
    @DeleteMapping("/{compartidoId}")
    public ResponseEntity<Void> revocar(@PathVariable Long procesoId,
                                        @PathVariable Long compartidoId,
                                        @RequestParam Long usuarioId) {
        procesoCompartidoService.revocar(procesoId, compartidoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar empresas colaboradoras", description = "Devuelve las colaboraciones activas del proceso.")
    @ApiResponse(responseCode = "200", description = "Colaboraciones consultadas correctamente.")
    @GetMapping
    public ResponseEntity<List<ProcesoCompartidoResponse>> listar(@PathVariable Long procesoId,
                                                                   @RequestParam Long usuarioId) {
        return ResponseEntity.ok(procesoCompartidoService.listar(procesoId, usuarioId));
    }
}
