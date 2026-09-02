package co.edu.javeriana.bmpn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.service.AutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class AutenticacionController {

    private final AutenticacionService autenticacionService;
    private final SesionHttp sesionHttp;

    public AutenticacionController(
            AutenticacionService autenticacionService,
            SesionHttp sesionHttp) {
        this.autenticacionService = autenticacionService;
        this.sesionHttp = sesionHttp;
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        if (!model.containsAttribute("formulario")) {
            model.addAttribute("formulario", new IniciarSesionRequest("", ""));
        }
        return "autenticacion/login";
    }

    @PostMapping("/login")
    public String iniciarSesion(
            @Valid @ModelAttribute("formulario") IniciarSesionRequest formulario,
            BindingResult resultado,
            HttpServletRequest request,
            Model model) {
        if (resultado.hasErrors()) {
            return "autenticacion/login";
        }

        try {
            SesionUsuarioResponse sesion = autenticacionService.iniciarSesion(formulario);
            sesionHttp.guardar(request, sesion);
            return "redirect:/usuarios";
        } catch (UnsupportedOperationException exception) {
            model.addAttribute("pendienteContrasena", exception.getMessage());
            return "autenticacion/login";
        }
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpServletRequest request) {
        sesionHttp.cerrar(request);
        return "redirect:/login?sesionCerrada";
    }
}
