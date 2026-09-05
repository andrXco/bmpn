package co.edu.javeriana.bmpn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bean de hasheo de contrasenas.
 *
 * Se define aqui, independiente de spring-security-web, para poder usarlo
 * ya mismo (entrega 1) sin necesitar la configuracion completa de Spring
 * Security (login, filtros, SecurityFilterChain), que llegara en la
 * entrega final. El contrato (interfaz PasswordEncoder) es el mismo, asi
 * que este bean se reutiliza sin cambios cuando se agregue la seguridad
 * completa.
 *
 * BCrypt ya incluye un salt aleatorio por contrasena (no hace falta
 * generarlo ni guardarlo aparte) y es deliberadamente lento para dificultar
 * ataques de fuerza bruta.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
