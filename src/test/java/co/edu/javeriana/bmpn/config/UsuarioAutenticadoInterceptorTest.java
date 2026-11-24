package co.edu.javeriana.bmpn.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;

class UsuarioAutenticadoInterceptorTest {

    private final UsuarioAutenticadoInterceptor interceptor = new UsuarioAutenticadoInterceptor();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final Object handler = new Object();
    private MockHttpServletRequest request;

    @BeforeEach
    void prepararPeticion() {
        request = new MockHttpServletRequest();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sinAutenticacionDejaPasar() {
        request.addParameter("usuarioId", "10");

        assertThat(interceptor.preHandle(request, response, handler)).isTrue();
    }

    @Test
    void autenticacionSinJwtDejaPasar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ana", null));
        request.addParameter("usuarioId", "10");

        assertThat(interceptor.preHandle(request, response, handler)).isTrue();
    }

    @Test
    void peticionSinUsuarioIdDejaPasar() {
        autenticarComo("10");

        assertThat(interceptor.preHandle(request, response, handler)).isTrue();
    }

    @Test
    void usuarioIdIgualAlDelTokenDejaPasar() {
        autenticarComo("10");
        request.addParameter("usuarioId", "10");

        assertThat(interceptor.preHandle(request, response, handler)).isTrue();
    }

    @Test
    void usuarioIdDistintoAlDelTokenFalla() {
        autenticarComo("10");
        request.addParameter("usuarioId", "11");

        assertThatThrownBy(() -> interceptor.preHandle(request, response, handler))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    private void autenticarComo(String usuarioId) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(usuarioId).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}