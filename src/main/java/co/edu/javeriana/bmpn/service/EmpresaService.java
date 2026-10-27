package co.edu.javeriana.bmpn.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.empresa.EmpresaResponse;
import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            UsuarioService usuarioService) {
        this.empresaRepository = empresaRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public EmpresaResponse registrar(RegistrarEmpresaRequest request) {
        String nit = request.getNit().trim().toUpperCase(Locale.ROOT);
        String emailAdministrador = normalizarEmail(request.getEmailAdministrador());

        if (empresaRepository.existsByNit(nit)) {
            throw new RecursoDuplicadoException("Ya existe una empresa con ese NIT");
        }
        if (usuarioService.existeEmail(emailAdministrador)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con ese correo");
        }

        Empresa empresa = new Empresa(
                nit,
                request.getNombreEmpresa().trim(),
                normalizarEmail(request.getCorreoContacto()));
        empresaRepository.save(empresa);

        // El administrador lo crea UsuarioService, que es el que maneja los usuarios
        UsuarioResponse administradorResponse = usuarioService.crearAdministrador(
                empresa,
                emailAdministrador,
                request.getNombreAdministrador().trim(),
                request.getApellidoAdministrador().trim(),
                request.getPassword());
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getNit(),
                empresa.getNombre(),
                empresa.getCorreoContacto(),
                empresa.getFechaCreacion(),
                empresa.isActivo(),
                administradorResponse);
    }

    // Para otros servicios que necesitan una empresa, sin usar su repositorio
    public Empresa buscarActiva(Long empresaId) {
        return empresaRepository.findByIdAndActivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
    }

    // Para el inicializador: solo carga datos de ejemplo si la base esta vacia
    @Transactional(readOnly = true)
    public boolean hayEmpresasRegistradas() {
        return empresaRepository.count() > 0;
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
