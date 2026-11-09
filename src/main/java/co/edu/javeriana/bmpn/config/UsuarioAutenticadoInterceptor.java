package co.edu.javeriana.bmpn.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.servlet.HandlerInterceptor;

import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UsuarioAutenticadoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof Jwt jwt)) {
            return true;
        }

        String[] usuariosSolicitantes = request.getParameterValues("usuarioId");
        if (usuariosSolicitantes == null) {
            return true;
        }

        for (String usuarioSolicitanteId : usuariosSolicitantes) {
            if (!jwt.getSubject().equals(usuarioSolicitanteId)) {
                throw new AccesoDenegadoException(
                        "El usuarioId de la solicitud no coincide con la sesión autenticada");
            }
        }
        return true;
    }
}
