package co.edu.javeriana.bmpn.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.Usuario;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(Usuario.class, UsuarioResponse.class)
                .addMappings(mapper -> mapper.map(
                        usuario -> usuario.getEmpresa().getId(),
                        UsuarioResponse::setEmpresaId));
        return modelMapper;
    }
}
