package co.edu.javeriana.bmpn.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class SesionHttp {

    private static final String HEADER_TOKEN = "X-Session-Token";

    private final Map<String, SesionUsuarioResponse> sesiones = new ConcurrentHashMap<>();

    public Optional<SesionUsuarioResponse> obtener(HttpServletRequest request) {
        String token = request.getHeader(HEADER_TOKEN);
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sesiones.get(token));
    }

    public void guardar(HttpServletRequest request, SesionUsuarioResponse sesion) {
        String tokenAnterior = request.getHeader(HEADER_TOKEN);
        if (tokenAnterior != null) {
            sesiones.remove(tokenAnterior);
        }

        String nuevoToken = UUID.randomUUID().toString();
        sesion.setToken(nuevoToken);
        sesiones.put(nuevoToken, sesion);
    }

    public void cerrar(HttpServletRequest request) {
        String token = request.getHeader(HEADER_TOKEN);
        if (token != null) {
            sesiones.remove(token);
        }
    }

}
