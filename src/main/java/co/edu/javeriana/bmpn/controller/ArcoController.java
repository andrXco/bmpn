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

import co.edu.javeriana.bmpn.dto.arco.ArcoResponse;
import co.edu.javeriana.bmpn.dto.arco.CrearArcoRequest;
import co.edu.javeriana.bmpn.dto.arco.EditarArcoRequest;
import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
import co.edu.javeriana.bmpn.service.ArcoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Arcos", description = "Secuencias entre elementos de un proceso BPMN.")
@RestController
@RequestMapping("/api/procesos/{procesoId}/arcos")
public class ArcoController {

    private final ArcoService arcoService;

    public ArcoController(ArcoService arcoService) {
        this.arcoService = arcoService;
    }

    @Operation(summary = "Crear arco", description = "Crea una secuencia entre dos elementos del mismo pool.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Arco creado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos o relación entre elementos inválida.")})
    @PostMapping
    public ResponseEntity<ArcoResponse> crear(@PathVariable Long procesoId,
                                              @RequestParam Long usuarioId,
                                              @Valid @RequestBody CrearArcoRequest request) {
        ArcoResponse arco = arcoService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/arcos/" + arco.getId());
        return ResponseEntity.created(ubicacion).body(arco);
    }

    @Operation(summary = "Listar arcos", description = "Devuelve los arcos activos de un proceso.")
    @ApiResponse(responseCode = "200", description = "Arcos consultados correctamente.")
    @GetMapping
    public ResponseEntity<List<ArcoResponse>> listar(@PathVariable Long procesoId,
                                                     @RequestParam Long usuarioId) {
        return ResponseEntity.ok(arcoService.listar(procesoId, usuarioId));
    }

    @Operation(summary = "Consultar arco", description = "Devuelve un arco activo del proceso.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Arco consultado correctamente."),
            @ApiResponse(responseCode = "404", description = "Arco no encontrado.")})
    @GetMapping("/{id}")
    public ResponseEntity<ArcoResponse> obtener(@PathVariable Long procesoId,
                                                @PathVariable Long id,
                                                @RequestParam Long usuarioId) {
        return ResponseEntity.ok(arcoService.obtener(procesoId, id, usuarioId));
    }

    @Operation(summary = "Editar arco", description = "Actualiza origen, destino, etiqueta o condición de un arco.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Arco actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos o relación entre elementos inválida."),
            @ApiResponse(responseCode = "404", description = "Arco no encontrado.")})
    @PutMapping("/{id}")
    public ResponseEntity<ArcoResponse> editar(@PathVariable Long procesoId,
                                               @PathVariable Long id,
                                               @RequestParam Long usuarioId,
                                               @Valid @RequestBody EditarArcoRequest request) {
        return ResponseEntity.ok(arcoService.editar(procesoId, id, usuarioId, request));
    }

    @Operation(summary = "Eliminar arco", description = "Realiza el borrado lógico del arco y devuelve advertencias del diagrama.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Arco eliminado correctamente."),
            @ApiResponse(responseCode = "404", description = "Arco no encontrado.")})
    @DeleteMapping("/{id}")
    public ResponseEntity<AdvertenciasResponse> eliminar(@PathVariable Long procesoId,
                                                         @PathVariable Long id,
                                                         @RequestParam Long usuarioId) {
        return ResponseEntity.ok(arcoService.eliminar(procesoId, id, usuarioId));
    }
}
