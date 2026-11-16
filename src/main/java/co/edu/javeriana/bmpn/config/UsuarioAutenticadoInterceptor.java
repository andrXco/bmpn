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
        validarUsuarioSolicitante(request);
        return true;
    }

    // Si la peticion trae ?usuarioId=, debe ser el mismo usuario del token; si no, responde 403
    private void validarUsuarioSolicitante(HttpServletRequest request) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        String[] usuariosSolicitantes = request.getParameterValues("usuarioId");
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof Jwt jwt)
                || usuariosSolicitantes == null) {
            return;
        }

        for (String usuarioSolicitanteId : usuariosSolicitantes) {
            if (!usuarioSolicitanteId.equals(jwt.getSubject())) {
                throw new AccesoDenegadoException(
                        "El usuarioId de la solicitud no coincide con la sesión autenticada");
            }
        }
    }
}
