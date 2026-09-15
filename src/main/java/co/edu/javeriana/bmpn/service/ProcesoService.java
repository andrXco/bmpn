package co.edu.javeriana.bmpn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.proceso.CrearProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.EditarProcesoRequest;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResponse;
import co.edu.javeriana.bmpn.entity.AccionHistorial;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.HistorialProceso;
import co.edu.javeriana.bmpn.entity.Pool;
import co.edu.javeriana.bmpn.entity.Proceso;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.TipoParticipante;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;
import co.edu.javeriana.bmpn.repository.EmpresaRepository;
import co.edu.javeriana.bmpn.repository.HistorialProcesoRepository;
import co.edu.javeriana.bmpn.repository.ProcesoRepository;
import co.edu.javeriana.bmpn.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class ProcesoService {

    private static final int ORDEN_POOL_PROPIETARIO = 0;

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialProcesoRepository;

    public ProcesoService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            ProcesoRepository procesoRepository,
            HistorialProcesoRepository historialProcesoRepository) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.procesoRepository = procesoRepository;
        this.historialProcesoRepository = historialProcesoRepository;
    }

    @Transactional
    public ProcesoResponse crear(
            Long empresaId,
            Long usuarioId,
            RolAcceso rolUsuarioAutenticado,
            CrearProcesoRequest request) {

        exigirPermisoDeEdicion(rolUsuarioAutenticado);

        String nombre = request.nombre().trim();
        if (procesoRepository.existsByEmpresaIdAndNombreIgnoreCase(empresaId, nombre)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un proceso con ese nombre en la empresa");
        }

        Empresa empresa = empresaRepository.findByIdAndActivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));

        Usuario autor = usuarioRepository
                .findByIdAndEmpresaIdAndActivoTrue(usuarioId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Proceso proceso = new Proceso(
                empresa,
                nombre,
                request.descripcion().trim(),
                request.categoria().trim());

        proceso.agregarPool(new Pool(
                empresa,
                empresa.getNombre(),
                TipoParticipante.EMPRESA_PROPIETARIA,
                ORDEN_POOL_PROPIETARIO));

        procesoRepository.save(proceso);

        historialProcesoRepository.save(new HistorialProceso(
                proceso,
                autor,
                AccionHistorial.CREACION,
                "Proceso creado en estado " + proceso.getEstado()));

        return ProcesoResponse.desde(proceso);
    }

    @Transactional
    public ProcesoResponse editar(
            Long procesoId,
            Long empresaId,
            Long usuarioId,
            RolAcceso rolUsuarioAutenticado,
            EditarProcesoRequest request) {

        exigirPermisoDeEdicion(rolUsuarioAutenticado);

        Proceso proceso = procesoRepository
                .findByIdAndEmpresaIdAndActivoTrue(procesoId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proceso no encontrado"));

        String nombre = request.nombre().trim();
        if (procesoRepository.existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(
                empresaId, nombre, procesoId)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un proceso con ese nombre en la empresa");
        }

        Usuario autor = usuarioRepository
                .findByIdAndEmpresaIdAndActivoTrue(usuarioId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        proceso.actualizarDatos(
                nombre,
                request.descripcion().trim(),
                request.categoria().trim(),
                request.estado());

        historialProcesoRepository.save(new HistorialProceso(
                proceso,
                autor,
                AccionHistorial.ACTUALIZACION,
                "Proceso actualizado; nuevo estado " + proceso.getEstado()));

        return ProcesoResponse.desde(proceso);
    }

    private void exigirPermisoDeEdicion(RolAcceso rol) {
        if (rol == RolAcceso.SOLO_LECTURA) {
            throw new AccesoDenegadoException(
                    "Un usuario de solo lectura no puede modificar procesos");
        }
    }
}