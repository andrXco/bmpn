package co.edu.javeriana.bmpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByEmailIgnoreCaseAndActivoTrue(String email);

    Optional<Usuario> findByIdAndEmpresaIdAndActivoTrue(Long id, Long empresaId);

    List<Usuario> findAllByEmpresaIdAndActivoTrueOrderByNombreAscApellidoAsc(Long empresaId);
}
