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

import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
import co.edu.javeriana.bmpn.dto.evento.CrearEventoRequest;
import co.edu.javeriana.bmpn.dto.evento.EditarEventoRequest;
import co.edu.javeriana.bmpn.dto.evento.EventoResponse;
import co.edu.javeriana.bmpn.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Eventos", description = "Eventos BPMN asociados a un proceso.")
@RestController
@RequestMapping("/api/procesos/{procesoId}/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @Operation(summary = "Crear evento", description = "Agrega un evento BPMN a un proceso.")
    @ApiResponse(responseCode = "201", description = "Evento creado correctamente.")
    @PostMapping
    public ResponseEntity<EventoResponse> crear(@PathVariable Long procesoId,
                                                @RequestParam Long usuarioId,
                                                @Valid @RequestBody CrearEventoRequest request) {
        EventoResponse evento = eventoService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/eventos/" + evento.getId());
        return ResponseEntity.created(ubicacion).body(evento);
    }

    @Operation(summary = "Listar eventos", description = "Devuelve los eventos activos de un proceso.")
    @ApiResponse(responseCode = "200", description = "Eventos consultados correctamente.")
    @GetMapping
    public ResponseEntity<List<EventoResponse>> listar(@PathVariable Long procesoId,
                                                       @RequestParam Long usuarioId) {
        return ResponseEntity.ok(eventoService.listar(procesoId, usuarioId));
    }

    @Operation(summary = "Consultar evento", description = "Devuelve un evento activo del proceso.")
    @ApiResponse(responseCode = "200", description = "Evento consultado correctamente.")
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> obtener(@PathVariable Long procesoId,
                                                  @PathVariable Long id,
                                                  @RequestParam Long usuarioId) {
        return ResponseEntity.ok(eventoService.obtener(procesoId, id, usuarioId));
    }

    @Operation(summary = "Editar evento", description = "Actualiza los datos y posición de un evento BPMN.")
    @ApiResponse(responseCode = "200", description = "Evento actualizado correctamente.")
    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> editar(@PathVariable Long procesoId,
                                                 @PathVariable Long id,
                                                 @RequestParam Long usuarioId,
                                                 @Valid @RequestBody EditarEventoRequest request) {
        return ResponseEntity.ok(eventoService.editar(procesoId, id, usuarioId, request));
    }

    @Operation(summary = "Eliminar evento", description = "Realiza el borrado lógico de un evento y devuelve advertencias.")
    @ApiResponse(responseCode = "200", description = "Evento eliminado correctamente.")
    @DeleteMapping("/{id}")
    public ResponseEntity<AdvertenciasResponse> eliminar(@PathVariable Long procesoId,
                                                         @PathVariable Long id,
                                                         @RequestParam Long usuarioId) {
        return ResponseEntity.ok(eventoService.eliminar(procesoId, id, usuarioId));
    }
}
