package co.edu.javeriana.bmpn.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.entity.Usuario;

@Service
public class TokenService {

    private static final String EMISOR = "bmpn";
    private static final String AUDIENCIA = "bmpn-api";

    private final JwtEncoder jwtEncoder;
    private final Duration expiracion;

    public TokenService(
            JwtEncoder jwtEncoder,
            @Value("${bmpn.security.jwt.access-expiration}") Duration expiracion) {
        if (expiracion.isZero() || expiracion.isNegative()) {
            throw new IllegalArgumentException("La vigencia del access token debe ser positiva");
        }
        this.jwtEncoder = jwtEncoder;
        this.expiracion = expiracion;
    }

    public SesionUsuarioResponse generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant vencimiento = ahora.plus(expiracion);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .audience(List.of(AUDIENCIA))
                .issuedAt(ahora)
                .expiresAt(vencimiento)
                .subject(usuario.getId().toString())
                .claim("token_type", "access")
                .claim("empresaId", usuario.getEmpresa().getId())
                .claim("rolAcceso", usuario.getRolAcceso().name())
                .build();

        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
        String accessToken = jwtEncoder
                .encode(JwtEncoderParameters.from(encabezado, claims))
                .getTokenValue();

        return new SesionUsuarioResponse(
                usuario.getId(),
                usuario.getEmpresa().getId(),
                usuario.getEmail(),
                usuario.getNombre() + " " + usuario.getApellido(),
                usuario.getRolAcceso(),
                accessToken,
                "Bearer",
                expiracion.toSeconds());
    }
}
