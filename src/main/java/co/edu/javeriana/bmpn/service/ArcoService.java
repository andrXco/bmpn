package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.arco.ArcoResponse;
import co.edu.javeriana.bmpn.dto.arco.CrearArcoRequest;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Arco;
import co.edu.javeriana.bmpn.entity.ElementoProceso;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.ArcoRepository;

@Service
public class ArcoService {

    private final ArcoRepository arcoRepository;
    private final ProcesoService procesoService;
    private final UsuarioService usuarioService;
    private final ElementoProcesoService elementoProcesoService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public ArcoService(ArcoRepository arcoRepository,
                       ProcesoService procesoService,
                       UsuarioService usuarioService,
                       ElementoProcesoService elementoProcesoService,
                       HistorialProcesoService historialProcesoService,
                       ModelMapper modelMapper) {
        this.arcoRepository = arcoRepository;
        this.procesoService = procesoService;
        this.usuarioService = usuarioService;
        this.elementoProcesoService = elementoProcesoService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public ArcoResponse crear(Long procesoId, Long usuarioId, CrearArcoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        if (request.getOrigenId().equals(request.getDestinoId())) {
            throw new SolicitudInvalidaException("Un arco no puede unir un elemento consigo mismo");
        }

        ElementoProceso origen = elementoProcesoService.buscarActivoDelProceso(request.getOrigenId(), procesoId);
        ElementoProceso destino = elementoProcesoService.buscarActivoDelProceso(request.getDestinoId(), procesoId);

        if (!origen.getPool().getId().equals(destino.getPool().getId())) {
            throw new SolicitudInvalidaException(
                    "Un arco no puede unir elementos de pools distintos");
        }

        String etiqueta = limpiarTexto(request.getEtiqueta());
        String condicion = limpiarTexto(request.getCondicion());

        // La tabla no permite dos filas con el mismo origen y destino, aunque una este inactiva
        Optional<Arco> existente = arcoRepository.findByProcesoIdAndOrigenIdAndDestinoId(
                procesoId, origen.getId(), destino.getId());
        Arco arco;
        if (existente.isPresent()) {
            arco = existente.get();
            if (arco.isActivo()) {
                throw new RecursoDuplicadoException("Ya existe un arco entre esos dos elementos");
            }
            arco.reactivar(etiqueta, condicion);
        } else {
            arco = new Arco(proceso, origen, destino, etiqueta, condicion);
            arcoRepository.save(arco);
        }

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                "Arco de '" + origen.getNombre() + "' a '" + destino.getNombre() + "' creado");
        return convertirAResponse(arco);
    }

    @Transactional(readOnly = true)
    public List<ArcoResponse> listar(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        List<ArcoResponse> respuesta = new ArrayList<>();
        for (Arco arco : arcoRepository.listarActivosPorProceso(procesoId)) {
            respuesta.add(convertirAResponse(arco));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public ArcoResponse obtener(Long procesoId, Long arcoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        return convertirAResponse(buscarActivo(arcoId, procesoId));
    }

    private Arco buscarActivo(Long arcoId, Long procesoId) {
        return arcoRepository.findByIdAndProcesoIdAndActivoTrue(arcoId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Arco no encontrado"));
    }

    // Un texto vacio o con solo espacios se guarda como null
    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private ArcoResponse convertirAResponse(Arco arco) {
        return modelMapper.map(arco, ArcoResponse.class);
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar arcos");
        }
    }
}