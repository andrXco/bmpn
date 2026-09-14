package co.edu.javeriana.bmpn.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;

@SpringBootTest
@Transactional
class RepositoryIntegrationTests {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void guardaYConsultaEmpresaYUsuarioRespetandoEstadoYEmpresa() {
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa(
                "900999999-TEST",
                "Empresa prueba repository",
                "contacto-repository@prueba.local"));

        Usuario usuario = usuarioRepository.saveAndFlush(new Usuario(
                empresa,
                "admin-repository@prueba.local",
                "Ada",
                "Lovelace",
                "$2a$10$hashExclusivoParaPruebaRepository",
                RolAcceso.ADMINISTRADOR));

        assertThat(empresaRepository.existsByNit("900999999-TEST")).isTrue();
        assertThat(empresaRepository.findByNit("900999999-TEST")).contains(empresa);
        assertThat(empresaRepository.findByIdAndActivoTrue(empresa.getId())).contains(empresa);

        assertThat(usuarioRepository.existsByEmailIgnoreCase(
                "ADMIN-REPOSITORY@PRUEBA.LOCAL")).isTrue();
        assertThat(usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(
                "ADMIN-REPOSITORY@PRUEBA.LOCAL")).contains(usuario);
        assertThat(usuarioRepository.findByIdAndEmpresaIdAndActivoTrue(
                usuario.getId(), empresa.getId())).contains(usuario);

        List<Usuario> usuariosActivos = usuarioRepository
                .findAllByEmpresaIdAndActivoTrueOrderByNombreAscApellidoAsc(empresa.getId());
        assertThat(usuariosActivos).containsExactly(usuario);

        usuario.desactivar();
        usuarioRepository.flush();

        assertThat(usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(usuario.getEmail()))
                .isEmpty();
        assertThat(usuarioRepository
                .findAllByEmpresaIdAndActivoTrueOrderByNombreAscApellidoAsc(empresa.getId()))
                .isEmpty();
    }
}
