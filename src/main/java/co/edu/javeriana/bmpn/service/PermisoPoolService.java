package co.edu.javeriana.bmpn.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.entity.PermisoPool;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.exception.SolicitudInvalidaException;
import co.edu.javeriana.bmpn.repository.PermisoPoolRepository;

// HU-24: que roles de acceso pueden crear, editar o eliminar la estructura de un pool
@Service
public class PermisoPoolService {

    private final PermisoPoolRepository permisoPoolRepository;

    public PermisoPoolService(PermisoPoolRepository permisoPoolRepository) {
        this.permisoPoolRepository = permisoPoolRepository;
    }

    @Transactional
    public PermisoPool definir(Pool pool, RolAcceso rolAcceso,
                               boolean puedeCrear, boolean puedeEditar, boolean puedeEliminar) {
        if (rolAcceso == RolAcceso.SOLO_LECTURA && (puedeCrear || puedeEditar || puedeEliminar)) {
            throw new SolicitudInvalidaException(
                    "Un rol de solo lectura no puede tener permisos de creacion, edicion o eliminacion");
        }

        PermisoPool permiso = permisoPoolRepository.buscarPorPoolYRol(pool.getId(), rolAcceso).orElse(null);
        if (permiso == null) {
            permiso = new PermisoPool(pool, rolAcceso, puedeCrear, puedeEditar, puedeEliminar);
            permisoPoolRepository.save(permiso);
        } else {
            permiso.actualizar(puedeCrear, puedeEditar, puedeEliminar);
        }
        return permiso;
    }

    @Transactional(readOnly = true)
    public List<PermisoPool> listar(Long poolId) {
        return permisoPoolRepository.listarPorPool(poolId);
    }
}
