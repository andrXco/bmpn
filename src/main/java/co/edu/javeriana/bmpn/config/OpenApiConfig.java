package co.edu.javeriana.bmpn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Metadatos de la especificación OpenAPI generada a partir de los controladores.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bpmnOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BPMN API")
                        .version("v1")
                        .description("API para la gestión académica de procesos BPMN."));
    }
}
