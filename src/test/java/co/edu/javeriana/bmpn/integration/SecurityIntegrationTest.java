package co.edu.javeriana.bmpn.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

// Pruebas de integracion de la API con Spring Security activo: se levanta la aplicacion completa
// (filtros de seguridad, controladores, servicios y PostgreSQL) y se llama a los endpoints con MockMvc.
// Cada prueba registra sus propias empresas; @Transactional deshace todo al terminar.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    private static final String PASSWORD = "ClaveSegura123";
    private static final String FRONTEND_ORIGIN = "http://localhost:4200";
    private static final String REFRESH_COOKIE = "bmpn_refresh";

    @Autowired
    private MockMvc mockMvc;

    // Empresa A con sus tres roles y empresa B con su administrador
    private Session adminA;
    private Session editorA;
    private Session readerA;
    private Session adminB;

    @BeforeEach
    void setUpCompanies() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        adminA = login(registerCompany("a" + suffix));
        adminB = login(registerCompany("b" + suffix));
        editorA = login(registerUser(adminA, "editor" + suffix + "@a.test", "EDITOR"));
        readerA = login(registerUser(adminA, "reader" + suffix + "@a.test", "SOLO_LECTURA"));
    }

    // ---- Autenticacion ----

    @Test
    void loginReturnsTokenAndRefreshCookie() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(adminA.email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.rolAcceso").value("ADMINISTRADOR"))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(REFRESH_COOKIE);
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(adminA.email, "WrongPassword1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/procesos").param("usuarioId", adminA.userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithTamperedTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/procesos")
                        .param("usuarioId", adminA.userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminA.token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userIdDifferentFromTokenReturns403() throws Exception {
        // El token es del administrador de A, pero intenta actuar como el de B
        mockMvc.perform(get("/api/procesos")
                        .param("usuarioId", adminB.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminA.bearer()))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshWithCookieReturnsNewToken() throws Exception {
        mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value(adminA.email));
    }

    @Test
    void reusingAnAlreadyUsedCookieReturns401() throws Exception {
        mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isOk());

        // El refresh token rota en cada renovacion: el anterior ya no sirve
        mockMvc.perform(post("/api/sesiones/renovacion")
                        .cookie(adminA.cookie)
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                .andExpect(status().isUnauthorized());
    }

    // ---- Aislamiento entre empresas ----

    @Test
    void otherCompanyCannotSeeProcessDetail() throws Exception {
        String processId = createProcess(adminA, "Compras");

        mockMvc.perform(get("/api/procesos/" + processId)
                        .param("usuarioId", adminB.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminB.bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listOnlyReturnsProcessesOfOwnCompany() throws Exception {
        createProcess(adminA, "Compras");

        mockMvc.perform(get("/api/procesos")
                        .param("usuarioId", adminA.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminA.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/procesos")
                        .param("usuarioId", adminB.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminB.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void otherCompanyCannotEditOrDeleteProcess() throws Exception {
        String processId = createProcess(adminA, "Compras");

        mockMvc.perform(put("/api/procesos/" + processId)
                        .param("usuarioId", adminB.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminB.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editData("Changed by B")))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/procesos/" + processId)
                        .param("usuarioId", adminB.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminB.bearer()))
                .andExpect(status().isNotFound());
    }

    // ---- Permisos por rol dentro de la empresa ----

    @Test
    void readerCannotCreateProcesses() throws Exception {
        mockMvc.perform(post("/api/procesos")
                        .param("usuarioId", readerA.userId)
                        .header(HttpHeaders.AUTHORIZATION, readerA.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processData("Compras")))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorCannotDeleteProcesses() throws Exception {
        String processId = createProcess(editorA, "Compras");

        mockMvc.perform(delete("/api/procesos/" + processId)
                        .param("usuarioId", editorA.userId)
                        .header(HttpHeaders.AUTHORIZATION, editorA.bearer()))
                .andExpect(status().isForbidden());
    }

    // ---- Caso de uso completo ----

    @Test
    void fullProcessLifecycle() throws Exception {
        String processId = createProcess(adminA, "Compras");

        mockMvc.perform(get("/api/procesos/" + processId)
                        .param("usuarioId", adminA.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminA.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Compras"))
                .andExpect(jsonPath("$.estado").value("BORRADOR"));

        mockMvc.perform(put("/api/procesos/" + processId)
                        .param("usuarioId", adminA.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminA.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editData("Compras nacionales")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Compras nacionales"))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));

        mockMvc.perform(delete("/api/procesos/" + processId)
                        .param("usuarioId", adminA.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminA.bearer()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/procesos")
                        .param("usuarioId", adminA.userId)
                        .header(HttpHeaders.AUTHORIZATION, adminA.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ---- Apoyo ----

    // Datos de una sesion iniciada
    private static class Session {
        private String email;
        private String userId;
        private String token;
        private Cookie cookie;

        private String bearer() {
            return "Bearer " + token;
        }
    }

    // Registra una empresa nueva y devuelve el correo de su administrador
    private String registerCompany(String suffix) throws Exception {
        String email = "admin" + suffix + "@company.test";
        String body = "{\"nit\":\"" + suffix + "\","
                + "\"nombreEmpresa\":\"Company " + suffix + "\","
                + "\"correoContacto\":\"contact" + suffix + "@company.test\","
                + "\"nombreAdministrador\":\"Ana\","
                + "\"apellidoAdministrador\":\"Test\","
                + "\"emailAdministrador\":\"" + email + "\","
                + "\"password\":\"" + PASSWORD + "\"}";
        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
        return email;
    }

    // El administrador registra a un colaborador de su empresa con el rol indicado
    private String registerUser(Session admin, String email, String role) throws Exception {
        String body = "{\"email\":\"" + email + "\","
                + "\"nombre\":\"Carlos\","
                + "\"apellido\":\"Test\","
                + "\"password\":\"" + PASSWORD + "\","
                + "\"rolAcceso\":\"" + role + "\"}";
        mockMvc.perform(post("/api/usuarios")
                        .param("usuarioId", admin.userId)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
        return email;
    }

    private Session login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();

        Session session = new Session();
        session.email = email;
        session.userId = JsonPath.read(json, "$.usuarioId").toString();
        session.token = JsonPath.read(json, "$.accessToken");
        session.cookie = result.getResponse().getCookie(REFRESH_COOKIE);
        return session;
    }

    // Crea un proceso y devuelve su id, revisando el 201 y la cabecera Location
    private String createProcess(Session session, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/procesos")
                        .param("usuarioId", session.userId)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processData(name)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
    }

    private String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String processData(String name) {
        return "{\"nombre\":\"" + name + "\","
                + "\"descripcion\":\"Test process\","
                + "\"categoria\":\"Testing\"}";
    }

    private String editData(String name) {
        return "{\"nombre\":\"" + name + "\","
                + "\"descripcion\":\"Test process\","
                + "\"categoria\":\"Testing\","
                + "\"estado\":\"PUBLICADO\"}";
    }
}