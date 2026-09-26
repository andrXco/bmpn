package co.edu.javeriana.bmpn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.entity.ElementoProceso;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.ElementoProcesoRepository;

@Service
public class ElementoProcesoService {

    private final ElementoProcesoRepository elementoProcesoRepository;

    public ElementoProcesoService(ElementoProcesoRepository elementoProcesoRepository) {
        this.elementoProcesoRepository = elementoProcesoRepository;
    }

    // Busca una actividad, gateway o evento activo que pertenezca al proceso
    @Transactional(readOnly = true)
    public ElementoProceso buscarActivoDelProceso(Long elementoId, Long procesoId) {
        return elementoProcesoRepository.findByIdAndProcesoIdAndActivoTrue(elementoId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Elemento no encontrado en el proceso"));
    }
}