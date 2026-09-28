package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.arco.ArcoResponse;
import co.edu.javeriana.bmpn.dto.arco.CrearArcoRequest;
import co.edu.javeriana.bmpn.dto.arco.EditarArcoRequest;
import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
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

    // HU-11
    @Transactional
    public ArcoResponse crear(Long procesoId, Long usuarioId, CrearArcoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        validarNoAutoarco(request.getOrigenId(), request.getDestinoId());
        ElementoProceso origen = elementoProcesoService.buscarActivoDelProceso(request.getOrigenId(), procesoId);
        ElementoProceso destino = elementoProcesoService.buscarActivoDelProceso(request.getDestinoId(), procesoId);
        validarMismoPool(origen, destino);
        validarInicioYFin(origen, destino);

        String etiqueta = limpiarTexto(request.getEtiqueta());
        String condicion = limpiarTexto(request.getCondicion());
        validarCondicion(origen, condicion);

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
                describir(origen, destino) + " creado");
        return convertirAResponse(arco);
    }

    // HU-12
    @Transactional
    public ArcoResponse editar(Long procesoId, Long arcoId, Long usuarioId, EditarArcoRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Arco arco = buscarActivo(arcoId, procesoId);

        validarNoAutoarco(request.getOrigenId(), request.getDestinoId());
        ElementoProceso origen = elementoProcesoService.buscarActivoDelProceso(request.getOrigenId(), procesoId);
        ElementoProceso destino = elementoProcesoService.buscarActivoDelProceso(request.getDestinoId(), procesoId);
        validarMismoPool(origen, destino);
        validarInicioYFin(origen, destino);

        String etiqueta = limpiarTexto(request.getEtiqueta());
        String condicion = limpiarTexto(request.getCondicion());
        validarCondicion(origen, condicion);

        // Otro arco (activo o eliminado) ya ocupa ese origen y destino
        Optional<Arco> existente = arcoRepository.findByProcesoIdAndOrigenIdAndDestinoId(
                procesoId, origen.getId(), destino.getId());
        if (existente.isPresent() && !existente.get().getId().equals(arcoId)) {
            throw new RecursoDuplicadoException("Ya existe un arco entre esos dos elementos");
        }

        arco.actualizar(origen, destino, etiqueta, condicion);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION,
                describir(origen, destino) + " actualizado");
        return convertirAResponse(arco);
    }

    // HU-13: la eliminacion es logica y avisa si algun elemento queda desconectado
    @Transactional
    public AdvertenciasResponse eliminar(Long procesoId, Long arcoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Arco arco = buscarActivo(arcoId, procesoId);

        arco.desactivar();

        List<String> advertencias = new ArrayList<>();
        advertirSiQuedaSinSalida(arco.getOrigen(), advertencias);
        advertirSiQuedaSinEntrada(arco.getDestino(), advertencias);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ELIMINACION,
                describir(arco.getOrigen(), arco.getDestino()) + " eliminado");
        return new AdvertenciasResponse(advertencias);
    }

    // Para ActividadService y GatewayService: al eliminar un elemento se eliminan sus arcos
    @Transactional
    public List<String> desactivarArcosDeElemento(Long elementoId) {
        List<Arco> arcos = arcoRepository.listarActivosDeElemento(elementoId);
        for (Arco arco : arcos) {
            arco.desactivar();
        }

        // Se revisan los vecinos del elemento eliminado, no el elemento mismo
        List<String> advertencias = new ArrayList<>();
        for (Arco arco : arcos) {
            if (!arco.getOrigen().getId().equals(elementoId)) {
                advertirSiQuedaSinSalida(arco.getOrigen(), advertencias);
            }
            if (!arco.getDestino().getId().equals(elementoId)) {
                advertirSiQuedaSinEntrada(arco.getDestino(), advertencias);
            }
        }
        return advertencias;
    }

    // Para PoolService: al eliminar un pool se eliminan los arcos de sus elementos
    @Transactional
    public void desactivarArcosDePool(Long poolId) {
        for (Arco arco : arcoRepository.listarActivosDePool(poolId)) {
            arco.desactivar();
        }
    }

    // Arcos que salen de un elemento
    @Transactional(readOnly = true)
    public List<Arco> listarSalidasActivas(Long elementoId) {
        return arcoRepository.findByOrigenIdAndActivoTrue(elementoId);
    }

    // Cuantos arcos llegan a un elemento
    @Transactional(readOnly = true)
    public long contarEntradasActivas(Long elementoId) {
        return arcoRepository.countByDestinoIdAndActivoTrue(elementoId);
    }
    
    // Al pasar un gateway a paralelo sus salidas pierden la condicion
    @Transactional
    public void quitarCondicionesDeSalidas(Long elementoId) {
        for (Arco arco : arcoRepository.findByOrigenIdAndActivoTrue(elementoId)) {
            arco.quitarCondicion();
        }
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

    private void advertirSiQuedaSinSalida(ElementoProceso elemento, List<String> advertencias) {
        if (!arcoRepository.existsByOrigenIdAndActivoTrue(elemento.getId())) {
            advertencias.add("El elemento '" + elemento.getNombre() + "' quedo sin camino de salida");
        }
    }

    private void advertirSiQuedaSinEntrada(ElementoProceso elemento, List<String> advertencias) {
        if (!arcoRepository.existsByDestinoIdAndActivoTrue(elemento.getId())) {
            advertencias.add("El elemento '" + elemento.getNombre() + "' quedo sin camino de entrada");
        }
    }

    private void validarNoAutoarco(Long origenId, Long destinoId) {
        if (origenId.equals(destinoId)) {
            throw new SolicitudInvalidaException("Un arco no puede unir un elemento consigo mismo");
        }
    }

    private void validarMismoPool(ElementoProceso origen, ElementoProceso destino) {
        if (!origen.getPool().getId().equals(destino.getPool().getId())) {
            throw new SolicitudInvalidaException(
                    "Un arco no puede unir elementos de pools distintos; esa comunicacion se modela como mensaje");
        }
    }

    // HU-27: un evento de inicio no recibe arcos y un evento de fin no tiene salidas
    private void validarInicioYFin(ElementoProceso origen, ElementoProceso destino) {
        if (!origen.aceptaArcosSalientes()) {
            throw new SolicitudInvalidaException("Un evento de fin no puede tener arcos de salida");
        }
        if (!destino.aceptaArcosEntrantes()) {
            throw new SolicitudInvalidaException("Un evento de inicio no puede tener arcos de entrada");
        }
    }

    // La condicion indica por que camino sigue el flujo: solo aplica si el origen decide un camino
    private void validarCondicion(ElementoProceso origen, String condicion) {
        if (condicion != null && !origen.aceptaCondiciones()) {
            throw new SolicitudInvalidaException(
                    "Solo un arco que sale de un gateway exclusivo o inclusivo puede tener condicion");
        }
    }

    // Un texto vacio o con solo espacios se guarda como null
    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    // Arco como se muestra en el historial, por ejemplo: Arco de 'Radicar' a 'Revisar'
    private String describir(ElementoProceso origen, ElementoProceso destino) {
        return "Arco de '" + origen.getNombre() + "' a '" + destino.getNombre() + "'";
    }

    private ArcoResponse convertirAResponse(Arco arco) {
        return modelMapper.map(arco, ArcoResponse.class);
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar arcos");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar arcos");
        }
    }
}