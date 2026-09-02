package co.edu.javeriana.bmpn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.dto.usuario.CambiarRolUsuarioRequest;
import co.edu.javeriana.bmpn.dto.usuario.RegistrarUsuarioRequest;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SesionHttp sesionHttp;

    public UsuarioController(UsuarioService usuarioService, SesionHttp sesionHttp) {
        this.usuarioService = usuarioService;
        this.sesionHttp = sesionHttp;
    }

    @GetMapping
    public String listar(HttpServletRequest request, Model model) {
        SesionUsuarioResponse sesion = sesionHttp.obtener(request).orElse(null);
        if (sesion == null) {
            return "redirect:/login";
        }
        prepararVista(model, sesion);
        return "usuario/lista";
    }

    @PostMapping
    public String registrar(
            @Valid @ModelAttribute("formulario") RegistrarUsuarioRequest formulario,
            BindingResult resultado,
            HttpServletRequest request,
            Model model) {
        SesionUsuarioResponse sesion = sesionHttp.obtener(request).orElse(null);
        if (sesion == null) {
            return "redirect:/login";
        }
        if (resultado.hasErrors()) {
            prepararVista(model, sesion);
            return "usuario/lista";
        }

        try {
            usuarioService.registrar(sesion.empresaId(), sesion.rolAcceso(), formulario);
            return "redirect:/usuarios?usuarioCreado";
        } catch (RecursoDuplicadoException | AccesoDenegadoException exception) {
            resultado.reject("usuario.invalido", exception.getMessage());
        } catch (UnsupportedOperationException exception) {
            model.addAttribute("pendienteContrasena", exception.getMessage());
        }
        prepararVista(model, sesion);
        return "usuario/lista";
    }

    @PostMapping("/{usuarioId}/rol")
    public String cambiarRol(
            @PathVariable Long usuarioId,
            @Valid @ModelAttribute CambiarRolUsuarioRequest formulario,
            BindingResult resultado,
            HttpServletRequest request) {
        SesionUsuarioResponse sesion = sesionHttp.obtener(request).orElse(null);
        if (sesion == null) {
            return "redirect:/login";
        }
        if (!resultado.hasErrors()) {
            try {
                usuarioService.cambiarRol(
                        sesion.empresaId(), sesion.rolAcceso(), usuarioId, formulario);
            } catch (AccesoDenegadoException | RecursoNoEncontradoException exception) {
                return "redirect:/usuarios?error";
            }
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{usuarioId}/desactivar")
    public String desactivar(@PathVariable Long usuarioId, HttpServletRequest request) {
        SesionUsuarioResponse sesion = sesionHttp.obtener(request).orElse(null);
        if (sesion == null) {
            return "redirect:/login";
        }
        try {
            usuarioService.desactivar(sesion.empresaId(), sesion.rolAcceso(), usuarioId);
            return "redirect:/usuarios?usuarioDesactivado";
        } catch (AccesoDenegadoException | RecursoNoEncontradoException exception) {
            return "redirect:/usuarios?error";
        }
    }

    private void prepararVista(Model model, SesionUsuarioResponse sesion) {
        model.addAttribute("sesion", sesion);
        model.addAttribute("usuarios", usuarioService.listarActivos(sesion.empresaId()));
        model.addAttribute("roles", RolAcceso.values());
        if (!model.containsAttribute("formulario")) {
            model.addAttribute("formulario",
                    new RegistrarUsuarioRequest("", "", "", "", RolAcceso.EDITOR));
        }
    }
}
