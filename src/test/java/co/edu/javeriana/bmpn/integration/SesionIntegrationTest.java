package co.edu.javeriana.bmpn.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

// Pruebas de integracion del ciclo de vida de la sesion: cierre, renovacion, deteccion de reuso
// del refresh token y validacion del origen. Complementan los casos de SecurityIntegrationTest.
class SesionIntegrationTest extends ApiIntegrationTest {

    @Test
    void loginTokenCarriesCompanyAndRole() {
        assertThat(adminA.role).isEqualTo("ADMINISTRADOR");
        assertThat(readerA.role).isEqualTo("SOLO_LECTURA");
        assertThat(readerA.companyId).isEqualTo(adminA.companyId);
        assertThat(adminB.companyId).isNotEqualTo(adminA.companyId);
    }

    @Test
    void loginWithUnknownEmailReturns401() throws Exception {
        mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("nadie" + suffix + "@x.test", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithInvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutRevokesRefreshTokenAndClearsCookie() throws Exception {
        MvcResult result = mockMvc.perform(delete("/api/sesiones")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isNoContent())
                .andReturn();

        Cookie cleared = result.getResponse().getCookie(REFRESH_COOKIE);
        assertThat(cleared).isNotNull();
        assertThat(cleared.getMaxAge()).isZero();

        // Despues del cierre, el refresh token ya no renueva la sesion
        mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reuseOfOldRefreshTokenRevokesTheWholeFamily() throws Exception {
        MvcResult renewed = mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isOk())
                .andReturn();
        Cookie newCookie = renewed.getResponse().getCookie(REFRESH_COOKIE);
        assertThat(newCookie.getValue()).isNotEqualTo(adminA.cookie.getValue());

        // Alguien reutiliza el token anterior (posible robo): se rechaza...
        mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isUnauthorized());

        // ...y tambien se invalida el token legitimo emitido en la renovacion
        mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(newCookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void renewedAccessTokenWorksOnProtectedEndpoints() throws Exception {
        MvcResult renewed = mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isOk())
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(
                renewed.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/procesos")
                        .param("usuarioId", adminA.userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void renewalWithoutCookieReturns401() throws Exception {
        mockMvc.perform(post("/api/sesiones/renovacion")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void renewalFromUnknownOriginReturns403() throws Exception {
        mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, "https://sitio-malicioso.test"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshTokenIsNotAcceptedAsAccessToken() throws Exception {
        mockMvc.perform(get("/api/procesos")
                        .param("usuarioId", adminA.userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminA.cookie.getValue()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        mockMvc.perform(get("/api/usuarios").param("usuarioId", adminA.userId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/roles-proceso").param("usuarioId", adminA.userId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/procesos").param("usuarioId", adminA.userId)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void companyRegistrationIsPublic() throws Exception {
        String email = registerCompany("c" + suffix);
        Session adminC = login(email);

        mockMvc.perform(as(adminC, get("/api/procesos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
