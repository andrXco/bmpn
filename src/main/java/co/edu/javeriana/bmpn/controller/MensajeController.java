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

import co.edu.javeriana.bmpn.dto.mensaje.CrearMensajeRequest;
import co.edu.javeriana.bmpn.dto.mensaje.MensajeResponse;
import co.edu.javeriana.bmpn.service.MensajeService;
import jakarta.validation.Valid;

// Mensajes y colaboracion entre pools
@RestController
@RequestMapping("/api/procesos/{procesoId}/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    //25 - 26
    @PostMapping
    public ResponseEntity<MensajeResponse> crear(@PathVariable Long procesoId,
                                                 @RequestParam Long usuarioId,
                                                 @Valid @RequestBody CrearMensajeRequest request) {
        MensajeResponse mensaje = mensajeService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/mensajes/" + mensaje.getId());
        return ResponseEntity.created(ubicacion).body(mensaje);
    }

    @GetMapping("/{mensajeId}")
    public ResponseEntity<MensajeResponse> obtener(@PathVariable Long procesoId,
                                                   @PathVariable Long mensajeId,
                                                   @RequestParam Long usuarioId) {
        return ResponseEntity.ok(mensajeService.obtener(procesoId, mensajeId, usuarioId));
    }

    @DeleteMapping("/{mensajeId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId,
                                         @PathVariable Long mensajeId,
                                         @RequestParam Long usuarioId) {
        mensajeService.eliminar(procesoId, mensajeId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MensajeResponse>> listar(@PathVariable Long procesoId,
                                                         @RequestParam Long usuarioId) {
        return ResponseEntity.ok(mensajeService.listar(procesoId, usuarioId));
    }

    //28
    @GetMapping("/correlacion/{clave}")
    public ResponseEntity<List<MensajeResponse>> listarPorCorrelacion(@PathVariable Long procesoId,
                                                                       @PathVariable String clave,
                                                                       @RequestParam Long usuarioId) {
        return ResponseEntity.ok(mensajeService.listarPorCorrelacion(procesoId, clave, usuarioId));
    }
}
