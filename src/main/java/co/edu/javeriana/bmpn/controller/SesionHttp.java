package co.edu.javeriana.bmpn.controller;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class SesionHttp {

    private static final String USUARIO_AUTENTICADO = "usuarioAutenticado";

    public Optional<SesionUsuarioResponse> obtener(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }

        Object valor = session.getAttribute(USUARIO_AUTENTICADO);
        if (valor instanceof SesionUsuarioResponse sesion) {
            return Optional.of(sesion);
        }
        return Optional.empty();
    }

    public void guardar(HttpServletRequest request, SesionUsuarioResponse sesion) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        request.getSession(true).setAttribute(USUARIO_AUTENTICADO, sesion);
    }

    public void cerrar(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

}
