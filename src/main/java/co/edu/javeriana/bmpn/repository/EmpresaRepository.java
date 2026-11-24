package co.edu.javeriana.bmpn.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.bmpn.entity.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByNit(String nit);

    Optional<Empresa> findByNit(String nit);

    Optional<Empresa> findByIdAndActivoTrue(Long id);
}
