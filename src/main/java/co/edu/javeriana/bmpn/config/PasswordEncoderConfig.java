package co.edu.javeriana.bmpn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bean de codificación de contraseñas utilizado por el inicio de sesión.
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
