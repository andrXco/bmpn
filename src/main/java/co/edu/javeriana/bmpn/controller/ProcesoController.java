package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import co.edu.javeriana.bmpn.dto.proceso.CrearProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.EditarProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.HistorialResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResumen;
import co.edu.javeriana.bmpn.entity.EstadoProceso;
import co.edu.javeriana.bmpn.service.ProcesoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/procesos")
@Tag(name = "Procesos", description = "Gestión de procesos BPMN de una empresa.")
public class ProcesoController {

    private final ProcesoService procesoService;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    @Operation(summary = "Crear proceso", description = "Crea un proceso BPMN para la empresa del usuario solicitante.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proceso creado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos para crear procesos.")
    })
    @PostMapping
    public ResponseEntity<ProcesoResponse> crear(
                                                 @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                                 @RequestParam Long usuarioId,
                                                 @Valid @RequestBody CrearProcesoRequest request) {
        ProcesoResponse proceso = procesoService.crear(usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + proceso.getId());
        return ResponseEntity.created(ubicacion).body(proceso);
    }

    @Operation(summary = "Editar proceso", description = "Actualiza la información y el estado de un proceso BPMN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proceso actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso no existe.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProcesoResponse> editar(
                                                  @Parameter(description = "Identificador del proceso.", example = "1")
                                                  @PathVariable Long id,
                                                  @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                                  @RequestParam Long usuarioId,
                                                  @Valid @RequestBody EditarProcesoRequest request) {
        return ResponseEntity.ok(procesoService.editar(id, usuarioId, request));
    }

    @Operation(summary = "Eliminar proceso", description = "Realiza el borrado lógico de un proceso BPMN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Proceso eliminado correctamente."),
            @ApiResponse(responseCode = "400", description = "Falta o es inválido algún identificador."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso no existe.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador del proceso.", example = "1") @PathVariable Long id,
            @Parameter(description = "Identificador del usuario solicitante.", example = "10") @RequestParam Long usuarioId) {
        procesoService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar procesos",
            description = "Lista los procesos de la empresa del usuario solicitante, con filtros y paginación opcionales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Procesos consultados correctamente."),
            @ApiResponse(responseCode = "400", description = "Algún parámetro es inválido."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo.")
    })
    @GetMapping
    public ResponseEntity<Page<ProcesoResumen>> listar(
            @Parameter(description = "Identificador del usuario solicitante.", example = "10")
            @RequestParam Long usuarioId,
            @Parameter(description = "Filtra por nombre; es opcional.", example = "compras")
            @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtra por estado; es opcional.", example = "PUBLICADO")
            @RequestParam(required = false) EstadoProceso estado,
            @Parameter(description = "Filtra por categoría; es opcional.", example = "Abastecimiento")
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest paginacion = PageRequest.of(page, size, Sort.by("nombre"));
        return ResponseEntity.ok(procesoService.listar(usuarioId, nombre, estado,
                categoria, incluirInactivos, paginacion));
    }

    @Operation(summary = "Consultar detalle de proceso", description = "Devuelve la información completa de un proceso BPMN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proceso consultado correctamente."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso no existe.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProcesoResponse> obtenerDetalle(
                                                          @Parameter(description = "Identificador del proceso.", example = "1") @PathVariable Long id,
                                                          @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                                          @RequestParam Long usuarioId) {
        return ResponseEntity.ok(procesoService.obtenerDetalle(id, usuarioId));
    }

    @Operation(summary = "Consultar historial de proceso", description = "Devuelve las modificaciones registradas para un proceso BPMN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial consultado correctamente."),
            @ApiResponse(responseCode = "401", description = "El usuario solicitante no existe o está inactivo."),
            @ApiResponse(responseCode = "403", description = "El usuario solicitante no tiene permisos sobre el proceso."),
            @ApiResponse(responseCode = "404", description = "El proceso no existe.")
    })
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialResponse>> obtenerHistorial(
                                                                    @Parameter(description = "Identificador del proceso.", example = "1") @PathVariable Long id,
                                                                    @Parameter(description = "Identificador del usuario solicitante.", example = "10")
                                                                    @RequestParam Long usuarioId) {
        return ResponseEntity.ok(procesoService.obtenerHistorial(id, usuarioId));
    }
}
