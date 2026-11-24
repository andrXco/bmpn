package co.edu.javeriana.bmpn.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.proceso.HistorialResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.HistorialProceso;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.repository.HistorialProcesoRepository;

@Service
public class HistorialProcesoService {

    private final HistorialProcesoRepository historialProcesoRepository;
    private final ModelMapper modelMapper;

    public HistorialProcesoService(HistorialProcesoRepository historialProcesoRepository,
                                   ModelMapper modelMapper) {
        this.historialProcesoRepository = historialProcesoRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public void registrar(Proceso proceso, Usuario usuario, AccionHistorial accion, String detalle) {
        HistorialProceso registro = new HistorialProceso(proceso, usuario, accion, detalle);
        historialProcesoRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<HistorialResponse> listarPorProceso(Long procesoId) {
        List<HistorialProceso> historial = historialProcesoRepository.listarPorProceso(procesoId);
        List<HistorialResponse> respuesta = new ArrayList<>();
        for (HistorialProceso registro : historial) {
            HistorialResponse item = modelMapper.map(registro, HistorialResponse.class);
            item.setNombreUsuario(registro.getUsuario().getNombre() + " " + registro.getUsuario().getApellido());
            item.setEmailUsuario(registro.getUsuario().getEmail());
            respuesta.add(item);
        }
        return respuesta;
    }
}