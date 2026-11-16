package co.edu.javeriana.bmpn.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.entity.RefreshToken;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;
import co.edu.javeriana.bmpn.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private static final String MENSAJE_SESION_INVALIDA = "La sesión no es válida o expiró";

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioService usuarioService;
    private final TokenService tokenService;
    private final Duration expiracion;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UsuarioService usuarioService,
            TokenService tokenService,
            @Value("${bmpn.security.refresh.expiration}") Duration expiracion) {
        if (expiracion.isZero() || expiracion.isNegative()) {
            throw new IllegalArgumentException("La vigencia del refresh token debe ser positiva");
        }
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
        this.expiracion = expiracion;
    }

    @Transactional
    public String crear(Long usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        if (!usuario.getEmpresa().isActivo()) {
            throw new AutenticacionRequeridaException(MENSAJE_SESION_INVALIDA);
        }
        return crearToken(usuario, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = AutenticacionRequeridaException.class)
    public TokenRenovado renovar(String tokenRecibido) {
        if (tokenRecibido == null || tokenRecibido.isBlank()) {
            throw new AutenticacionRequeridaException(MENSAJE_SESION_INVALIDA);
        }

        RefreshToken token = refreshTokenRepository
                .findByTokenHashForUpdate(calcularHash(tokenRecibido))
                .orElseThrow(() -> new AutenticacionRequeridaException(MENSAJE_SESION_INVALIDA));

        Instant ahora = Instant.now();
        if (token.isRevocado() || !token.getFechaExpiracion().isAfter(ahora)) {
            revocarFamilia(token.getFamiliaId(), ahora);
            throw new AutenticacionRequeridaException(MENSAJE_SESION_INVALIDA);
        }

        Usuario usuario = token.getUsuario();
        if (!usuario.isActivo() || !usuario.getEmpresa().isActivo()) {
            revocarFamilia(token.getFamiliaId(), ahora);
            throw new AutenticacionRequeridaException(MENSAJE_SESION_INVALIDA);
        }

        token.revocar(ahora);
        String nuevoRefreshToken = crearToken(usuario, token.getFamiliaId());
        return new TokenRenovado(tokenService.generar(usuario), nuevoRefreshToken);
    }

    @Transactional
    public void revocar(String tokenRecibido) {
        if (tokenRecibido == null || tokenRecibido.isBlank()) {
            return;
        }

        refreshTokenRepository.findByTokenHashForUpdate(calcularHash(tokenRecibido))
                .ifPresent(token -> revocarFamilia(token.getFamiliaId(), Instant.now()));
    }

    private String crearToken(Usuario usuario, UUID familiaId) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken entidad = new RefreshToken(
                usuario,
                calcularHash(token),
                familiaId,
                Instant.now().plus(expiracion));
        refreshTokenRepository.save(entidad);
        return token;
    }

    private void revocarFamilia(UUID familiaId, Instant ahora) {
        List<RefreshToken> tokensActivos = refreshTokenRepository
                .findAllByFamiliaIdAndFechaRevocacionIsNull(familiaId);
        tokensActivos.forEach(token -> token.revocar(ahora));
    }

    private String calcularHash(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible", exception);
        }
    }

    public record TokenRenovado(SesionUsuarioResponse sesion, String refreshToken) {
    }
}
