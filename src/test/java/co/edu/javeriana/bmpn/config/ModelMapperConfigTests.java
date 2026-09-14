package co.edu.javeriana.bmpn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;

class ModelMapperConfigTests {

    @Test
    void debeMapearUsuarioAUsuarioResponse() {
        Empresa empresa = new Empresa("900123456", "BMPN", "contacto@bmpn.test");
        Usuario usuario = new Usuario(
                empresa,
                "usuario@bmpn.test",
                "Ana",
                "Perez",
                "hash-seguro",
                RolAcceso.ADMINISTRADOR);
        ModelMapper modelMapper = new ModelMapperConfig().modelMapper();

        UsuarioResponse response = modelMapper.map(usuario, UsuarioResponse.class);

        assertEquals("usuario@bmpn.test", response.getEmail());
        assertEquals("Ana", response.getNombre());
        assertEquals(RolAcceso.ADMINISTRADOR, response.getRolAcceso());
        assertTrue(response.isActivo());
    }
}
