package co.edu.javeriana.bmpn.controller;

import java.net.URI;

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

import co.edu.javeriana.bmpn.dto.rolproceso.CrearRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.EditarRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.RolProcesoResponse;
import co.edu.javeriana.bmpn.service.RolProcesoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

//Catalogo de roles de la empresa
//17 a 20
@Tag(name = "Roles de proceso", description = "Catálogo de responsabilidades funcionales de una empresa.")
@RestController
@RequestMapping("/api/roles-proceso")
public class RolProcesoController {

    private final RolProcesoService rolProcesoService;

    public RolProcesoController(RolProcesoService rolProcesoService) {
        this.rolProcesoService = rolProcesoService;
    }

    //17
    @Operation(summary = "Crear rol de proceso", description = "Crea un rol funcional disponible para los procesos de la empresa.")
    @ApiResponse(responseCode = "201", description = "Rol creado correctamente.")
    @PostMapping
    public ResponseEntity<RolProcesoResponse> crear(@RequestParam Long usuarioId,
                                                     @Valid @RequestBody CrearRolProcesoRequest request) {
        RolProcesoResponse rol = rolProcesoService.crear(usuarioId, request);
        return ResponseEntity.created(URI.create("/api/roles-proceso/" + rol.getId())).body(rol);
    }

    //18
    @Operation(summary = "Editar rol de proceso", description = "Actualiza un rol funcional de la empresa.")
    @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente.")
    @PutMapping("/{id}")
    public ResponseEntity<RolProcesoResponse> editar(@PathVariable Long id,
                                                      @RequestParam Long usuarioId,
                                                      @Valid @RequestBody EditarRolProcesoRequest request) {
        return ResponseEntity.ok(rolProcesoService.editar(id, usuarioId, request));
    }

    //19
    @Operation(summary = "Eliminar rol de proceso", description = "Realiza el borrado lógico de un rol funcional.")
    @ApiResponse(responseCode = "204", description = "Rol eliminado correctamente.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam Long usuarioId) {
        rolProcesoService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    //20
    @Operation(summary = "Listar roles de proceso", description = "Lista roles funcionales con filtros y paginación opcionales.")
    @ApiResponse(responseCode = "200", description = "Roles consultados correctamente.")
    @GetMapping
    public ResponseEntity<Page<RolProcesoResponse>> listar(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest paginacion = PageRequest.of(page, size, Sort.by("nombre"));
        return ResponseEntity.ok(rolProcesoService.listar(usuarioId, nombre, incluirInactivos, paginacion));
    }

    @Operation(summary = "Consultar rol de proceso", description = "Devuelve un rol funcional de la empresa.")
    @ApiResponse(responseCode = "200", description = "Rol consultado correctamente.")
    @GetMapping("/{id}")
    public ResponseEntity<RolProcesoResponse> obtener(@PathVariable Long id, @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolProcesoService.obtener(id, usuarioId));
    }
}
