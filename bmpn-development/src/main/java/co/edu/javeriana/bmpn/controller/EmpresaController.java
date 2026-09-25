package co.edu.javeriana.bmpn.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.bmpn.dto.empresa.EmpresaResponse;
import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.service.EmpresaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> registrar(
            @Valid @RequestBody RegistrarEmpresaRequest formulario) {
        EmpresaResponse empresa = empresaService.registrar(formulario);
        return ResponseEntity
                .created(URI.create("/api/empresas/" + empresa.getId()))
                .body(empresa);
    }
}
