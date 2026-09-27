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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import co.edu.javeriana.bmpn.dto.actividad.ActividadResponse;
import co.edu.javeriana.bmpn.dto.actividad.CrearActividadRequest;
import co.edu.javeriana.bmpn.dto.actividad.EditarActividadRequest;
import co.edu.javeriana.bmpn.service.ActividadService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/procesos/{procesoId}/actividades")
@Tag(name = "Actividades", description = "Actividades contenidas en un proceso BPMN.")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @Operation(summary = "Crear actividad", description = "Agrega una actividad BPMN a un proceso de la empresa del usuario solicitante.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Actividad creada correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso no existe.")
    })
    @PostMapping
    public ResponseEntity<ActividadResponse> crear(
                                                   @Parameter(description = "Identificador del proceso.", example = "1") @PathVariable Long procesoId,
                                                   @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                                   @RequestParam Long usuarioId,
                                                   @Valid @RequestBody CrearActividadRequest request) {
        ActividadResponse actividad = actividadService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/actividades/" + actividad.getId());
        return ResponseEntity.created(ubicacion).body(actividad);
    }

    @Operation(summary = "Listar actividades", description = "Devuelve las actividades de un proceso BPMN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividades consultadas correctamente."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso no existe.")
    })
    @GetMapping
    public ResponseEntity<List<ActividadResponse>> listar(
                                                          @Parameter(description = "Identificador del proceso.", example = "1") @PathVariable Long procesoId,
                                                          @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                                          @RequestParam Long usuarioId) {
        return ResponseEntity.ok(actividadService.listar(procesoId, usuarioId));
    }

    @Operation(summary = "Consultar actividad", description = "Devuelve una actividad BPMN de un proceso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad consultada correctamente."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso o la actividad no existen.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ActividadResponse> obtener(
                                                     @Parameter(description = "Identificador del proceso.", example = "1") @PathVariable Long procesoId,
                                                     @Parameter(description = "Identificador de la actividad.", example = "1") @PathVariable Long id,
                                                     @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                                     @RequestParam Long usuarioId) {
        return ResponseEntity.ok(actividadService.obtener(procesoId, id, usuarioId));
    }

    @Operation(summary = "Editar actividad", description = "Actualiza los datos y posición de una actividad BPMN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad actualizada correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso o la actividad no existen.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ActividadResponse> editar(
                                                    @Parameter(description = "Identificador del proceso.", example = "1") @PathVariable Long procesoId,
                                                    @Parameter(description = "Identificador de la actividad.", example = "1") @PathVariable Long id,
                                                    @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                                    @RequestParam Long usuarioId,
                                                    @Valid @RequestBody EditarActividadRequest request) {
        return ResponseEntity.ok(actividadService.editar(procesoId, id, usuarioId, request));
    }

    @Operation(summary = "Eliminar actividad", description = "Elimina una actividad BPMN de un proceso.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actividad eliminada correctamente."),
            @ApiResponse(responseCode = "400", description = "Falta o es inválido algún identificador."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso o la actividad no existen.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
                                         @Parameter(description = "Identificador del proceso.", example = "1") @PathVariable Long procesoId,
                                         @Parameter(description = "Identificador de la actividad.", example = "1") @PathVariable Long id,
                                         @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                         @RequestParam Long usuarioId) {
        actividadService.eliminar(procesoId, id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
