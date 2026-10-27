package co.edu.javeriana.bmpn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Permite que el frontend Angular, que corre en otro origen (http://localhost:4200), llame a la API.
// Los origenes se cambian con la variable CORS_ORIGENES, separados por coma.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public CorsConfig(@Value("${app.cors.origenes}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location");
    }
}
