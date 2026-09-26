package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.gateway.CrearGatewayRequest;
import co.edu.javeriana.bmpn.dto.gateway.EditarGatewayRequest;
import co.edu.javeriana.bmpn.dto.gateway.GatewayResponse;
import co.edu.javeriana.bmpn.dto.diagrama.AdvertenciasResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Arco;
import co.edu.javeriana.bmpn.entity.Gateway;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoGateway;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.GatewayRepository;

@Service
public class GatewayService {

    private final GatewayRepository gatewayRepository;
    private final ProcesoService procesoService;
    private final UsuarioService usuarioService;
    private final ElementoProcesoService elementoProcesoService;
    private final ArcoService arcoService;
    private final HistorialProcesoService historialProcesoService;
    private final ModelMapper modelMapper;

    public GatewayService(GatewayRepository gatewayRepository,
                          ProcesoService procesoService,
                          UsuarioService usuarioService,
                          ElementoProcesoService elementoProcesoService,
                          ArcoService arcoService,
                          HistorialProcesoService historialProcesoService,
                          ModelMapper modelMapper) {
        this.gatewayRepository = gatewayRepository;
        this.procesoService = procesoService;
        this.usuarioService = usuarioService;
        this.elementoProcesoService = elementoProcesoService;
        this.arcoService = arcoService;
        this.historialProcesoService = historialProcesoService;
        this.modelMapper = modelMapper;
    }

    // HU-14
    @Transactional
    public GatewayResponse crear(Long procesoId, Long usuarioId, CrearGatewayRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        Pool pool = elementoProcesoService.buscarPoolParaElemento(proceso, request.getPoolId());
        String nombre = request.getNombre().trim();
        Gateway gateway = new Gateway(proceso, pool, nombre, request.getTipoGateway(),
                request.getPosicionX(), request.getPosicionY());
        gatewayRepository.save(gateway);

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.CREACION,
                "Gateway '" + nombre + "' de tipo " + gateway.getTipoGateway() + " creado");
        return convertirAResponse(gateway);
    }

    // HU-15
    @Transactional
    public GatewayResponse editar(Long procesoId, Long gatewayId, Long usuarioId,
                                  EditarGatewayRequest request) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeEdicion(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Gateway gateway = buscarActivo(gatewayId, procesoId);

        TipoGateway tipoAnterior = gateway.getTipoGateway();
        String nombre = request.getNombre().trim();
        gateway.renombrar(nombre);
        gateway.cambiarTipo(request.getTipoGateway());
        gateway.mover(request.getPosicionX(), request.getPosicionY());

        // Al pasar a paralelo se siguen todos los caminos, asi que las condiciones sobran
        if (gateway.getTipoGateway() == TipoGateway.PARALELO && tipoAnterior != TipoGateway.PARALELO) {
            arcoService.quitarCondicionesDeSalidas(gatewayId);
        }

        String detalle = "Gateway '" + nombre + "' actualizado";
        if (tipoAnterior != gateway.getTipoGateway()) {
            detalle = "Gateway '" + nombre + "' cambio de " + tipoAnterior + " a " + gateway.getTipoGateway();
        }
        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ACTUALIZACION, detalle);
        return convertirAResponse(gateway);
    }

    // HU-16: la eliminacion es logica y arrastra los arcos del gateway
    @Transactional
    public AdvertenciasResponse eliminar(Long procesoId, Long gatewayId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        exigirPermisoDeAdministrador(usuario.getRolAcceso());
        Proceso proceso = procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        Gateway gateway = buscarActivo(gatewayId, procesoId);

        // Se cuentan las ramas antes de desactivar los arcos
        int ramas = arcoService.listarSalidasActivas(gatewayId).size();

        gateway.desactivar();
        List<String> advertencias = new ArrayList<>();
        if (ramas >= 2) {
            advertencias.add("La ramificacion del gateway '" + gateway.getNombre()
                    + "' quedo sin punto de decision: " + ramas + " caminos se quedaron sin origen");
        }
        advertencias.addAll(arcoService.desactivarArcosDeElemento(gatewayId));

        historialProcesoService.registrar(proceso, usuario, AccionHistorial.ELIMINACION,
                "Gateway '" + gateway.getNombre() + "' eliminado");
        return new AdvertenciasResponse(advertencias);
    }

    @Transactional(readOnly = true)
    public List<GatewayResponse> listar(Long procesoId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());

        List<GatewayResponse> respuesta = new ArrayList<>();
        for (Gateway gateway : gatewayRepository.listarActivosPorProceso(procesoId)) {
            respuesta.add(convertirAResponse(gateway));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public GatewayResponse obtener(Long procesoId, Long gatewayId, Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        procesoService.buscarActivoDeEmpresa(procesoId, usuario.getEmpresa().getId());
        return convertirAResponse(buscarActivo(gatewayId, procesoId));
    }

    private Gateway buscarActivo(Long gatewayId, Long procesoId) {
        return gatewayRepository.findByIdAndProcesoIdAndActivoTrue(gatewayId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Gateway no encontrado"));
    }

    // Reglas de HU-14 y HU-15: se revisan como advertencias porque en borrador el diagrama puede estar incompleto
    private List<String> revisarCoherencia(Gateway gateway) {
        List<String> advertencias = new ArrayList<>();
        List<Arco> salidas = arcoService.listarSalidasActivas(gateway.getId());
        long entradas = arcoService.contarEntradasActivas(gateway.getId());

        // Si junta varios caminos en uno solo, no divide el flujo y estas reglas no aplican
        boolean juntaElFlujo = entradas >= 2 && salidas.size() <= 1;
        if (juntaElFlujo) {
            return advertencias;
        }

        if (salidas.size() < 2) {
            advertencias.add("El gateway '" + gateway.getNombre()
                    + "' divide el flujo y necesita al menos dos arcos salientes; tiene " + salidas.size());
        }
        if (gateway.getTipoGateway() != TipoGateway.PARALELO) {
            for (Arco salida : salidas) {
                if (salida.getCondicion() == null) {
                    advertencias.add("El arco hacia '" + salida.getDestino().getNombre()
                            + "' sale de un gateway " + gateway.getTipoGateway() + " y no tiene condicion");
                }
            }
        }
        if (gateway.getTipoGateway() == TipoGateway.EXCLUSIVO) {
            advertirCondicionesRepetidas(gateway, salidas, advertencias);
        }
        return advertencias;
    }

    // En un exclusivo solo se toma un camino: dos salidas con la misma condicion no se distinguen
    private void advertirCondicionesRepetidas(Gateway gateway, List<Arco> salidas, List<String> advertencias) {
        List<String> condicionesVistas = new ArrayList<>();
        for (Arco salida : salidas) {
            if (salida.getCondicion() != null) {
                String condicion = salida.getCondicion().trim().toLowerCase(Locale.ROOT);
                if (condicionesVistas.contains(condicion)) {
                    advertencias.add("En el gateway exclusivo '" + gateway.getNombre()
                            + "' hay salidas con la misma condicion '" + salida.getCondicion()
                            + "'; no son mutuamente excluyentes");
                } else {
                    condicionesVistas.add(condicion);
                }
            }
        }
    }

    private GatewayResponse convertirAResponse(Gateway gateway) {
        GatewayResponse respuesta = modelMapper.map(gateway, GatewayResponse.class);
        respuesta.setAdvertencias(revisarCoherencia(gateway));
        return respuesta;
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException("Un usuario de solo lectura no puede modificar gateways");
        }
    }

    private void exigirPermisoDeAdministrador(RolAcceso rol) {
        if (rol != RolAcceso.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar gateways");
        }
    }
}