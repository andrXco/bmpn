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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/procesos/{procesoId}/gateways")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping
    public ResponseEntity<GatewayResponse> crear(@PathVariable Long procesoId,
                                                 @RequestParam Long usuarioId,
                                                 @Valid @RequestBody CrearGatewayRequest request) {
        GatewayResponse gateway = gatewayService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/gateways/" + gateway.getId());
        return ResponseEntity.created(ubicacion).body(gateway);
    }

    @GetMapping
    public ResponseEntity<List<GatewayResponse>> listar(@PathVariable Long procesoId,
                                                        @RequestParam Long usuarioId) {
        return ResponseEntity.ok(gatewayService.listar(procesoId, usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GatewayResponse> obtener(@PathVariable Long procesoId,
                                                   @PathVariable Long id,
                                                   @RequestParam Long usuarioId) {
        return ResponseEntity.ok(gatewayService.obtener(procesoId, id, usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GatewayResponse> editar(@PathVariable Long procesoId,
                                                  @PathVariable Long id,
                                                  @RequestParam Long usuarioId,
                                                  @Valid @RequestBody EditarGatewayRequest request) {
        return ResponseEntity.ok(gatewayService.editar(procesoId, id, usuarioId, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<AdvertenciasResponse> eliminar(@PathVariable Long procesoId,
                                                         @PathVariable Long id,
                                                         @RequestParam Long usuarioId) {
        return ResponseEntity.ok(gatewayService.eliminar(procesoId, id, usuarioId));
    }
}