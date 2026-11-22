package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Test
    void rechazaUnaVigenciaQueNoSeaPositiva() {
        Duration negativa = Duration.ofMinutes(-1);

        assertThatThrownBy(() -> new TokenService(jwtEncoder, negativa))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarArmaLaSesionConElToken() {
        Empresa empresa = new Empresa("900123", "Empresa Prueba", "contacto@empresa.test");
        ReflectionTestUtils.setField(empresa, "id", 3L);
        Usuario usuario = new Usuario(empresa, "ana@empresa.test", "Ana", "Prueba", "hash",
                RolAcceso.EDITOR);
        ReflectionTestUtils.setField(usuario, "id", 10L);
        Jwt jwt = Jwt.withTokenValue("token-firmado").header("alg", "HS256").subject("10").build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        SesionUsuarioResponse sesion = new TokenService(jwtEncoder, Duration.ofMinutes(15)).generar(usuario);

        assertThat(sesion.getAccessToken()).isEqualTo("token-firmado");
        assertThat(sesion.getTokenType()).isEqualTo("Bearer");
        assertThat(sesion.getExpiresIn()).isEqualTo(900L);
        assertThat(sesion.getNombreCompleto()).isEqualTo("Ana Prueba");

        // El subject del JWT es el id del usuario y lleva la empresa y el rol como claims
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();
        assertThat(claims.getSubject()).isEqualTo("10");
        assertThat((Long) claims.getClaim("empresaId")).isEqualTo(3L);
        assertThat((String) claims.getClaim("rolAcceso")).isEqualTo("EDITOR");
    }
}