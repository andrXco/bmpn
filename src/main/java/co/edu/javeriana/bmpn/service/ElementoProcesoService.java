package co.edu.javeriana.bmpn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.entity.ElementoProceso;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
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

    // Pool donde se va a ubicar un elemento, si no llega poolId se usa el de la empresa propietaria
    @Transactional(readOnly = true)
    public Pool buscarPoolParaElemento(Proceso proceso, Long poolId) {
        Pool encontrado = null;
        for (Pool pool : proceso.getPools()) {
            if (poolId == null && pool.esPropietario() && pool.isActivo()) {
                encontrado = pool;
            }
            if (poolId != null && poolId.equals(pool.getId()) && pool.isActivo()) {
                encontrado = pool;
            }
        }
        if (encontrado == null) {
            throw new RecursoNoEncontradoException("Pool no encontrado en el proceso");
        }
        if (encontrado.isCajaNegra()) {
            throw new SolicitudInvalidaException(
                    "Un pool de caja negra no puede tener elementos internos");
        }
        return encontrado;
    }
}