package co.edu.javeriana.bmpn.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
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

import co.edu.javeriana.bmpn.dto.pool.AsociarRolPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.CrearPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.EditarPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.PermisoPoolRequest;
import co.edu.javeriana.bmpn.dto.pool.PermisoPoolResponse;
import co.edu.javeriana.bmpn.dto.pool.PoolResponse;
import co.edu.javeriana.bmpn.dto.pool.RolPoolResponse;
import co.edu.javeriana.bmpn.service.PoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Pools de un proceso
//21, 22, 24
@Tag(name = "Pools", description = "Participantes y permisos de un proceso BPMN.")
@RestController
@RequestMapping("/api/procesos/{procesoId}/pools")
public class PoolController {

    private final PoolService poolService;

    public PoolController(PoolService poolService) {
        this.poolService = poolService;
    }

    //21
    @Operation(summary = "Crear pool", description = "Agrega un participante al proceso.")
    @ApiResponse(responseCode = "201", description = "Pool creado correctamente.")
    @PostMapping
    public ResponseEntity<PoolResponse> crear(@PathVariable Long procesoId,
                                              @RequestParam Long usuarioId,
                                              @Valid @RequestBody CrearPoolRequest request) {
        PoolResponse pool = poolService.crear(procesoId, usuarioId, request);
        URI ubicacion = URI.create("/api/procesos/" + procesoId + "/pools/" + pool.getId());
        return ResponseEntity.created(ubicacion).body(pool);
    }

    @Operation(summary = "Editar pool", description = "Actualiza los datos de un pool.")
    @ApiResponse(responseCode = "200", description = "Pool actualizado correctamente.")
    @PutMapping("/{poolId}")
    public ResponseEntity<PoolResponse> editar(@PathVariable Long procesoId,
                                               @PathVariable Long poolId,
                                               @RequestParam Long usuarioId,
                                               @Valid @RequestBody EditarPoolRequest request) {
        return ResponseEntity.ok(poolService.editar(procesoId, poolId, usuarioId, request));
    }

    @Operation(summary = "Eliminar pool", description = "Realiza el borrado lógico de un pool.")
    @ApiResponse(responseCode = "204", description = "Pool eliminado correctamente.")
    @DeleteMapping("/{poolId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId,
                                         @PathVariable Long poolId,
                                         @RequestParam Long usuarioId) {
        poolService.eliminar(procesoId, poolId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    //22(se listan junto al pool para verlo con sus lanes desde el mismo recurso)
    @Operation(summary = "Listar pools", description = "Devuelve los pools activos del proceso.")
    @ApiResponse(responseCode = "200", description = "Pools consultados correctamente.")
    @GetMapping
    public ResponseEntity<List<PoolResponse>> listar(@PathVariable Long procesoId,
                                                      @RequestParam Long usuarioId) {
        return ResponseEntity.ok(poolService.listar(procesoId, usuarioId));
    }

    @Operation(summary = "Consultar pool", description = "Devuelve un pool con sus datos asociados.")
    @ApiResponse(responseCode = "200", description = "Pool consultado correctamente.")
    @GetMapping("/{poolId}")
    public ResponseEntity<PoolResponse> obtener(@PathVariable Long procesoId,
                                                @PathVariable Long poolId,
                                                @RequestParam Long usuarioId) {
        return ResponseEntity.ok(poolService.obtener(procesoId, poolId, usuarioId));
    }

    //24 (roles para el pool)
    @Operation(summary = "Asociar rol disponible", description = "Asocia un rol de proceso a un pool.")
    @ApiResponse(responseCode = "201", description = "Rol asociado correctamente.")
    @PostMapping("/{poolId}/roles-disponibles")
    public ResponseEntity<RolPoolResponse> asociarRol(@PathVariable Long procesoId,
                                                       @PathVariable Long poolId,
                                                       @RequestParam Long usuarioId,
                                                       @Valid @RequestBody AsociarRolPoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(poolService.asociarRol(procesoId, poolId, usuarioId, request));
    }

    @Operation(summary = "Desvincular rol disponible", description = "Quita un rol de proceso del pool.")
    @ApiResponse(responseCode = "204", description = "Rol desvinculado correctamente.")
    @DeleteMapping("/{poolId}/roles-disponibles/{rolProcesoId}")
    public ResponseEntity<Void> desvincularRol(@PathVariable Long procesoId,
                                               @PathVariable Long poolId,
                                               @PathVariable Long rolProcesoId,
                                               @RequestParam Long usuarioId) {
        poolService.desvincularRol(procesoId, poolId, rolProcesoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar roles disponibles", description = "Devuelve los roles de proceso asociados al pool.")
    @ApiResponse(responseCode = "200", description = "Roles consultados correctamente.")
    @GetMapping("/{poolId}/roles-disponibles")
    public ResponseEntity<List<RolPoolResponse>> listarRolesDisponibles(@PathVariable Long procesoId,
                                                                        @PathVariable Long poolId,
                                                                        @RequestParam Long usuarioId) {
        return ResponseEntity.ok(poolService.listarRolesDisponibles(procesoId, poolId, usuarioId));
    }

    //24 (permisos por pool)
    @Operation(summary = "Definir permiso de pool", description = "Define el permiso de una empresa sobre un pool compartido.")
    @ApiResponse(responseCode = "201", description = "Permiso definido correctamente.")
    @PostMapping("/{poolId}/permisos")
    public ResponseEntity<PermisoPoolResponse> definirPermiso(@PathVariable Long procesoId,
                                                               @PathVariable Long poolId,
                                                               @RequestParam Long usuarioId,
                                                               @Valid @RequestBody PermisoPoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(poolService.definirPermiso(procesoId, poolId, usuarioId, request));
    }

    @Operation(summary = "Listar permisos de pool", description = "Devuelve los permisos definidos para un pool.")
    @ApiResponse(responseCode = "200", description = "Permisos consultados correctamente.")
    @GetMapping("/{poolId}/permisos")
    public ResponseEntity<List<PermisoPoolResponse>> listarPermisos(@PathVariable Long procesoId,
                                                                     @PathVariable Long poolId,
                                                                     @RequestParam Long usuarioId) {
        return ResponseEntity.ok(poolService.listarPermisos(procesoId, poolId, usuarioId));
    }
}
