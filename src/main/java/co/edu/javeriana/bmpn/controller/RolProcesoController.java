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

import co.edu.javeriana.bmpn.dto.rolproceso.CrearRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.EditarRolProcesoRequest;
import co.edu.javeriana.bmpn.dto.rolproceso.RolProcesoResponse;
import co.edu.javeriana.bmpn.service.RolProcesoService;
import jakarta.validation.Valid;

//Catalogo de roles de la empresa
//17 a 20
@RestController
@RequestMapping("/api/roles-proceso")
public class RolProcesoController {

    private final RolProcesoService rolProcesoService;

    public RolProcesoController(RolProcesoService rolProcesoService) {
        this.rolProcesoService = rolProcesoService;
    }

    //17
    @PostMapping
    public ResponseEntity<RolProcesoResponse> crear(@RequestParam Long usuarioId,
                                                     @Valid @RequestBody CrearRolProcesoRequest request) {
        RolProcesoResponse rol = rolProcesoService.crear(usuarioId, request);
        return ResponseEntity.created(URI.create("/api/roles-proceso/" + rol.getId())).body(rol);
    }

    //18
    @PutMapping("/{id}")
    public ResponseEntity<RolProcesoResponse> editar(@PathVariable Long id,
                                                      @RequestParam Long usuarioId,
                                                      @Valid @RequestBody EditarRolProcesoRequest request) {
        return ResponseEntity.ok(rolProcesoService.editar(id, usuarioId, request));
    }

    //19
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam Long usuarioId) {
        rolProcesoService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    //20
    @GetMapping
    public ResponseEntity<List<RolProcesoResponse>> listar(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return ResponseEntity.ok(rolProcesoService.listar(usuarioId, incluirInactivos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolProcesoResponse> obtener(@PathVariable Long id, @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolProcesoService.obtener(id, usuarioId));
    }
}