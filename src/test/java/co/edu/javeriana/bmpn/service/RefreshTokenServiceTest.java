package co.edu.javeriana.bmpn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.javeriana.bmpn.dto.autenticacion.SesionUsuarioResponse;
import co.edu.javeriana.bmpn.entity.Empresa;
import co.edu.javeriana.bmpn.entity.RefreshToken;
import co.edu.javeriana.bmpn.entity.RolAcceso;
import co.edu.javeriana.bmpn.entity.Usuario;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;
import co.edu.javeriana.bmpn.repository.RefreshTokenRepository;
import co.edu.javeriana.bmpn.service.RefreshTokenService.TokenRenovado;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Long USUARIO_ID = 10L;
    private static final Duration VIGENCIA = Duration.ofDays(5);

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private TokenService tokenService;

    private RefreshTokenService refreshTokenService;

    private Empresa empresa;
    private Usuario usuario;

    @BeforeEach
    void prepararDatos() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository, usuarioService, tokenService, VIGENCIA);
        empresa = new Empresa("900123", "Empresa Prueba", "contacto@empresa.test");
        usuario = new Usuario(empresa, "ana@empresa.test", "Ana", "Prueba", "hash",
                RolAcceso.ADMINISTRADOR);
    }

    @Test
    void rechazaUnaVigenciaQueNoSeaPositiva() {
        assertThatThrownBy(() -> new RefreshTokenService(
                refreshTokenRepository, usuarioService, tokenService, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearGuardaSoloElHashDelToken() {
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);

        String token = refreshTokenService.crear(USUARIO_ID);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken guardado = captor.getValue();

        // En la base de datos queda el SHA-256 en hexadecimal, nunca el token
        assertThat(token).isNotBlank();
        assertThat(guardado.getTokenHash()).hasSize(64).isNotEqualTo(token);
        assertThat(guardado.getUsuario()).isSameAs(usuario);
        assertThat(guardado.getFamiliaId()).isNotNull();
        assertThat(guardado.getFechaCreacion()).isNotNull();
        assertThat(guardado.getFechaExpiracion()).isAfter(Instant.now());
        assertThat(guardado.isRevocado()).isFalse();
    }

    @Test
    void crearConEmpresaInactivaFalla() {
        empresa.desactivar();
        when(usuarioService.buscarActivo(USUARIO_ID)).thenReturn(usuario);

        assertThatThrownBy(() -> refreshTokenService.crear(USUARIO_ID))
                .isInstanceOf(AutenticacionRequeridaException.class);
    }

    @Test
    void renovarSinTokenFalla() {
        assertThatThrownBy(() -> refreshTokenService.renovar(null))
                .isInstanceOf(AutenticacionRequeridaException.class);
        assertThatThrownBy(() -> refreshTokenService.renovar("  "))
                .isInstanceOf(AutenticacionRequeridaException.class);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void renovarTokenDesconocidoFalla() {
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.renovar("token-desconocido"))
                .isInstanceOf(AutenticacionRequeridaException.class);
    }

    @Test
    void renovarTokenVencidoRevocaLaFamilia() {
        RefreshToken vencido = tokenGuardado(Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(vencido));
        when(refreshTokenRepository.findAllByFamiliaIdAndFechaRevocacionIsNull(vencido.getFamiliaId()))
                .thenReturn(List.of(vencido));

        assertThatThrownBy(() -> refreshTokenService.renovar("token-vencido"))
                .isInstanceOf(AutenticacionRequeridaException.class);
        assertThat(vencido.isRevocado()).isTrue();
    }

    @Test
    void renovarConUsuarioInactivoRevocaLaFamilia() {
        usuario.desactivar();
        RefreshToken token = tokenGuardado(Instant.now().plus(VIGENCIA));
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));
        when(refreshTokenRepository.findAllByFamiliaIdAndFechaRevocacionIsNull(token.getFamiliaId()))
                .thenReturn(List.of(token));

        assertThatThrownBy(() -> refreshTokenService.renovar("token-valido"))
                .isInstanceOf(AutenticacionRequeridaException.class);
        assertThat(token.isRevocado()).isTrue();
    }

    @Test
    void renovarRotaElTokenYConservaLaFamilia() {
        RefreshToken anterior = tokenGuardado(Instant.now().plus(VIGENCIA));
        SesionUsuarioResponse sesion = new SesionUsuarioResponse();
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(anterior));
        when(tokenService.generar(usuario)).thenReturn(sesion);

        TokenRenovado renovado = refreshTokenService.renovar("token-valido");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(renovado.sesion()).isSameAs(sesion);
        assertThat(renovado.refreshToken()).isNotBlank();
        assertThat(anterior.isRevocado()).isTrue();
        assertThat(captor.getValue().getFamiliaId()).isEqualTo(anterior.getFamiliaId());
    }

    @Test
    void revocarSinTokenNoHaceNada() {
        refreshTokenService.revocar(null);
        refreshTokenService.revocar("");

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void revocarCierraTodaLaFamilia() {
        RefreshToken actual = tokenGuardado(Instant.now().plus(VIGENCIA));
        RefreshToken otro = tokenGuardado(Instant.now().plus(VIGENCIA));
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(actual));
        when(refreshTokenRepository.findAllByFamiliaIdAndFechaRevocacionIsNull(actual.getFamiliaId()))
                .thenReturn(List.of(actual, otro));

        refreshTokenService.revocar("token-valido");

        assertThat(actual.isRevocado()).isTrue();
        assertThat(otro.isRevocado()).isTrue();
    }

    @Test
    void revocarDosVecesConservaLaPrimeraFecha() {
        RefreshToken token = tokenGuardado(Instant.now().plus(VIGENCIA));
        Instant primeraFecha = Instant.now();

        token.revocar(primeraFecha);
        token.revocar(primeraFecha.plusSeconds(30));

        assertThat(token.getFechaRevocacion()).isEqualTo(primeraFecha);
    }

    private RefreshToken tokenGuardado(Instant fechaExpiracion) {
        return new RefreshToken(usuario, "hash", UUID.randomUUID(), fechaExpiracion);
    }
}