package co.edu.javeriana.bmpn.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.empresa.EmpresaResponse;
import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;
import co.edu.javeriana.bmpn.repository.UsuarioRepository;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EmpresaResponse registrar(RegistrarEmpresaRequest request) {
        String nit = request.getNit().trim().toUpperCase(Locale.ROOT);
        String emailAdministrador = normalizarEmail(request.getEmailAdministrador());

        if (empresaRepository.existsByNit(nit)) {
            throw new RecursoDuplicadoException("Ya existe una empresa con ese NIT");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(emailAdministrador)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con ese correo");
        }

        throw new UnsupportedOperationException(
                "El registro se completara cuando se implemente el manejo seguro de contrasenas");
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
