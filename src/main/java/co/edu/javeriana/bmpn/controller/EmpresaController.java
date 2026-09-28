package co.edu.javeriana.bmpn.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import co.edu.javeriana.bmpn.dto.empresa.EmpresaResponse;
import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.service.EmpresaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Registro de empresas y su administrador inicial.")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @Operation(
            summary = "Registrar empresa",
            description = "Crea una empresa y su usuario administrador inicial.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa registrada correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "409", description = "El NIT o alguno de los correos ya está registrado.")
    })
    @PostMapping
    public ResponseEntity<EmpresaResponse> registrar(
            @Valid @RequestBody RegistrarEmpresaRequest formulario) {
        EmpresaResponse empresa = empresaService.registrar(formulario);
        return ResponseEntity
                .created(URI.create("/api/empresas/" + empresa.getId()))
                .body(empresa);
    }
}
