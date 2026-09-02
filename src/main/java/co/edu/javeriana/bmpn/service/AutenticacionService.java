package co.edu.javeriana.bmpn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.autenticacion.IniciarSesionRequest;
import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;

@Service
public class AutenticacionService {

    @Transactional(readOnly = true)
    public SesionUsuarioResponse iniciarSesion(IniciarSesionRequest request) {
        throw new UnsupportedOperationException(
                "El inicio de sesion se completara cuando se implemente el manejo seguro de contrasenas");
    }
}
