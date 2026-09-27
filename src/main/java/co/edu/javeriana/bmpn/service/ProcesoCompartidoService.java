package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.procesocompartido.CompartirProcesoRequest;
import co.edu.javeriana.bmpn.dto.procesocompartido.ProcesoCompartidoResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.ProcesoCompartido;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;
import co.edu.javeriana.bmpn.repository.ProcesoCompartidoRepository;

//23 da a otra empresa la capacidad para participar como pool en un proceso ajeno. 
//PoolService consulta estaCompartidoCon(...)
@Service
public class ProcesoCompartidoService {

    private final ProcesoCompartidoRepository procesoCompartidoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final ProcesoService procesoService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public ProcesoCompartidoService(ProcesoCompartidoRepository procesoCompartidoRepository,
                                    EmpresaRepository empresaRepository,
                                    UsuarioService usuarioService,
                                    ProcesoService procesoService,
                                    HistorialProcesoService historialProcesoService,
                                    ModelMapper modelMapper) {
        this.procesoCompartidoRepository = procesoCompartidoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioService = usuarioService;
        this.procesoService = procesoService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public ProcesoCompartidoResponse compartir(Long procesoId, Long usuarioId, CompartirProcesoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        Empresa empresaInvitada = empresaRepository.findByIdAndActivoTrue(request.getEmpresaInvitadaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa invitada no encontrada"));

        if (empresaInvitada.getId().equals(usuario.getEmpresa().getId())) {
            throw new SolicitudInvalidaException("Una empresa no puede invitarse a su propio proceso");
        }

        ProcesoCompartido compartido = procesoCompartidoRepository
                .findByProcesoIdAndEmpresaInvitadaId(procesoId, empresaInvitada.getId())
                .orElse(null);
        if (compartido != null) {
            if (compartido.isActivo()) {
                throw new RecursoDuplicadoException("La empresa ya fue invitada a este proceso");
            }
            compartido.activar();
        } else {
            compartido = new ProcesoCompartido(proceso, empresaInvitada, usuario);
            procesoCompartidoRepository.save(compartido);
        }

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                "Proceso compartido con la empresa '" + empresaInvitada.getNombre() + "'");
        return convertirAResponse(compartido);
    }

    @Transactional
    public void revocar(Long procesoId, Long compartidoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        ProcesoCompartido compartido = procesoCompartidoRepository
                .findByIdAndProcesoIdAndActivoTrue(compartidoId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de colaboracion no encontrado"));
        compartido.desactivar();

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                "Se revoco la colaboracion con la empresa '"
                        + compartido.getEmpresaInvitada().getNombre() + "'");
    }

    @Transactional(readOnly = true)
    public List<ProcesoCompartidoResponse> listar(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        List<ProcesoCompartidoResponse> respuesta = new ArrayList<>();
        for (ProcesoCompartido compartido : procesoCompartidoRepository.findAllByProcesoIdAndActivoTrue(procesoId)) {
            respuesta.add(convertirAResponse(compartido));
        }
        return respuesta;
    }

    public boolean estaCompartidoCon(Long procesoId, Long empresaId) {
        return procesoCompartidoRepository.findByProcesoIdAndEmpresaInvitadaId(procesoId, empresaId)
                .map(ProcesoCompartido::isActivo)
                .orElse(false);
    }

    private ProcesoCompartidoResponse convertirAResponse(ProcesoCompartido compartido) {
        ProcesoCompartidoResponse respuesta = modelMapper.map(compartido, ProcesoCompartidoResponse.class);
        respuesta.setNombreEmpresaInvitada(compartido.getEmpresaInvitada().getNombre());
        return respuesta;
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede compartir un proceso");
        }
    }
}