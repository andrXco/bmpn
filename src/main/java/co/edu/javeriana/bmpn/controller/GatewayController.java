package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import co.edu.javeriana.bmpn.dto.gateway.CrearGatewayRequest;
import co.edu.javeriana.bmpn.dto.gateway.EditarGatewayRequest;
import co.edu.javeriana.bmpn.dto.gateway.GatewayResponse;
import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
import co.edu.javeriana.bmpn.service.GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Gateways", description = "Puntos de decisión y ramificación de un proceso BPMN.")
@RestController
@RequestMapping("/api/procesos/{procesoId}/gateways")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @Operation(summary = "Crear gateway", description = "Agrega un gateway a un pool del proceso.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Gateway creado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos.")})
    @PostMapping
    public ResponseEntity<GatewayResponse> crear(@PathVariable Long procesoId,
                                                 @RequestParam Long usuarioId,
                                                 @Valid @RequestBody CrearGatewayRequest request) {
        GatewayResponse gateway = gatewayService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/gateways/" + gateway.getId());
        return ResponseEntity.created(ubicacion).body(gateway);
    }

    @Operation(summary = "Listar gateways", description = "Devuelve los gateways activos del proceso.")
    @ApiResponse(responseCode = "200", description = "Gateways consultados correctamente.")
    @GetMapping
    public ResponseEntity<List<GatewayResponse>> listar(@PathVariable Long procesoId,
                                                        @RequestParam Long usuarioId) {
        return ResponseEntity.ok(gatewayService.listar(procesoId, usuarioId));
    }

    @Operation(summary = "Consultar gateway", description = "Devuelve un gateway activo del proceso.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gateway consultado correctamente."),
            @ApiResponse(responseCode = "404", description = "Gateway no encontrado.")})
    @GetMapping("/{id}")
    public ResponseEntity<GatewayResponse> obtener(@PathVariable Long procesoId,
                                                   @PathVariable Long id,
                                                   @RequestParam Long usuarioId) {
        return ResponseEntity.ok(gatewayService.obtener(procesoId, id, usuarioId));
    }

    @Operation(summary = "Editar gateway", description = "Actualiza los datos y la posición de un gateway.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gateway actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "404", description = "Gateway no encontrado.")})
    @PutMapping("/{id}")
    public ResponseEntity<GatewayResponse> editar(@PathVariable Long procesoId,
                                                  @PathVariable Long id,
                                                  @RequestParam Long usuarioId,
                                                  @Valid @RequestBody EditarGatewayRequest request) {
        return ResponseEntity.ok(gatewayService.editar(procesoId, id, usuarioId, request));
    }
    
    @Operation(summary = "Eliminar gateway", description = "Realiza el borrado lógico del gateway y devuelve advertencias del diagrama.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gateway eliminado correctamente."),
            @ApiResponse(responseCode = "404", description = "Gateway no encontrado.")})
    @DeleteMapping("/{id}")
    public ResponseEntity<AdvertenciasResponse> eliminar(@PathVariable Long procesoId,
                                                         @PathVariable Long id,
                                                         @RequestParam Long usuarioId) {
        return ResponseEntity.ok(gatewayService.eliminar(procesoId, id, usuarioId));
    }
}
