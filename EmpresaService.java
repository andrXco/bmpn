package co.edu.javeriana.bmpn.service;

import java.util.Locale;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.empresa.EmpresaResponse;
import co.edu.javeriana.bmpn.dto.empresa.RegistrarEmpresaRequest;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;
import co.edu.javeriana.bmpn.repository.UsuarioRepository;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            ModelMapper modelMapper) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
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

        Empresa empresa = new Empresa(
                nit,
                request.getNombreEmpresa().trim(),
                normalizarEmail(request.getCorreoContacto()));
        empresaRepository.save(empresa);

        // request.getPassword() (texto plano) solo se lee una vez, aqui, para
        // producir el hash. Nunca se guarda en una variable propia ni viaja
        // mas alla de esta linea: de esta entidad en adelante solo existe
        // passwordHash.
        Usuario administrador = new Usuario(
                empresa,
                emailAdministrador,
                request.getNombreAdministrador().trim(),
                request.getApellidoAdministrador().trim(),
                passwordEncoder.encode(request.getPassword()),
                RolAcceso.ADMINISTRADOR);
        usuarioRepository.save(administrador);

        UsuarioResponse administradorResponse = modelMapper.map(administrador, UsuarioResponse.class);
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getNit(),
                empresa.getNombre(),
                empresa.getCorreoContacto(),
                empresa.getFechaCreacion(),
                empresa.isActivo(),
                administradorResponse);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
