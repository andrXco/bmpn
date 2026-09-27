package co.edu.javeriana.bmpn.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.javeriana.bmpn.dto.actividad.ActividadResponse;
import co.edu.javeriana.bmpn.dto.arco.ArcoResponse;
import co.edu.javeriana.bmpn.dto.gateway.GatewayResponse;
import co.edu.javeriana.bmpn.dto.proceso.ProcesoResponse;
import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
import co.edu.javeriana.bmpn.entity.Actividad;
import co.edu.javeriana.bmpn.entity.Arco;
import co.edu.javeriana.bmpn.entity.Gateway;
import co.edu.javeriana.bmpn.entity.Proceso;
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
        modelMapper.typeMap(Proceso.class, ProcesoResponse.class)
                .addMappings(mapper -> mapper.map(
                        proceso -> proceso.getEmpresa().getId(),
                        ProcesoResponse::setEmpresaId));
        modelMapper.typeMap(Actividad.class, ActividadResponse.class)
                .addMappings(mapper -> {
                    mapper.map(actividad -> actividad.getProceso().getId(),
                            ActividadResponse::setProcesoId);
                    mapper.map(actividad -> actividad.getPool().getId(),
                            ActividadResponse::setPoolId);
                    mapper.map(actividad -> actividad.getLane().getId(),
                            ActividadResponse::setLaneId);
                });
        modelMapper.typeMap(Arco.class, ArcoResponse.class)
                .addMappings(mapper -> {
                    mapper.map(arco -> arco.getProceso().getId(),
                            ArcoResponse::setProcesoId);
                    mapper.map(arco -> arco.getOrigen().getId(),
                            ArcoResponse::setOrigenId);
                    mapper.map(arco -> arco.getDestino().getId(),
                            ArcoResponse::setDestinoId);
                });
        modelMapper.typeMap(Gateway.class, GatewayResponse.class)
                .addMappings(mapper -> {
                    mapper.map(gateway -> gateway.getProceso().getId(),
                            GatewayResponse::setProcesoId);
                    mapper.map(gateway -> gateway.getPool().getId(),
                            GatewayResponse::setPoolId);
                });
        return modelMapper;
    }
}