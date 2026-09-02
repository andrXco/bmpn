package co.edu.javeriana.bmpn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.service.EmpresaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        if (!model.containsAttribute("formulario")) {
            model.addAttribute("formulario", formularioVacio());
        }
        return "empresa/registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute("formulario") RegistrarEmpresaRequest formulario,
            BindingResult resultado,
            Model model) {
        if (resultado.hasErrors()) {
            return "empresa/registro";
        }

        try {
            empresaService.registrar(formulario);
            return "redirect:/login?registroExitoso";
        } catch (RecursoDuplicadoException exception) {
            resultado.reject("registro.duplicado", exception.getMessage());
        } catch (UnsupportedOperationException exception) {
            model.addAttribute("pendienteContrasena", exception.getMessage());
        }
        return "empresa/registro";
    }

    private RegistrarEmpresaRequest formularioVacio() {
        return new RegistrarEmpresaRequest("", "", "", "", "", "", "");
    }
}
