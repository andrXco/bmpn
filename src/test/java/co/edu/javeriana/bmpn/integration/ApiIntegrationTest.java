package co.edu.javeriana.bmpn.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

// Base de las pruebas de integracion de la API: levanta la aplicacion completa con Spring Security
// y PostgreSQL. Antes de cada prueba se registran dos empresas: A (admin, editor y lector) y B (admin).
// @Transactional deshace todos los datos al terminar cada prueba.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class ApiIntegrationTest {

    protected static final String PASSWORD = "ClaveSegura123";
    protected static final String FRONTEND_ORIGIN = "http://localhost:4200";
    protected static final String REFRESH_COOKIE = "bmpn_refresh";

    @Autowired
    protected MockMvc mockMvc;

    protected Session adminA;
    protected Session editorA;
    protected Session readerA;
    protected Session adminB;
    protected String suffix;

    @BeforeEach
    void setUpCompanies() throws Exception {
        suffix = String.valueOf(System.nanoTime());
        adminA = login(registerCompany("a" + suffix));
        adminB = login(registerCompany("b" + suffix));
        editorA = login(registerUser(adminA, "editor" + suffix + "@a.test", "EDITOR"));
        readerA = login(registerUser(adminA, "reader" + suffix + "@a.test", "SOLO_LECTURA"));
    }

    // ---- Datos de una sesion iniciada ----

    protected static class Session {
        protected String email;
        protected String userId;
        protected String companyId;
        protected String role;
        protected String token;
        protected Cookie cookie;

        protected String bearer() {
            return "Bearer " + token;
        }
    }

    // ---- Peticiones autenticadas ----

    // Agrega el token y el usuarioId de la sesion a cualquier peticion
    protected MockHttpServletRequestBuilder as(Session session, MockHttpServletRequestBuilder request) {
        return request
                .param("usuarioId", session.userId)
                .header(HttpHeaders.AUTHORIZATION, session.bearer());
    }

    protected MockHttpServletRequestBuilder asJson(Session session, MockHttpServletRequestBuilder request,
                                                   String body) {
        return as(session, request).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    // ---- Preparacion de datos ----

    protected String registerCompany(String companySuffix) throws Exception {
        String email = "admin" + companySuffix + "@company.test";
        String body = "{\"nit\":\"" + companySuffix + "\","
                + "\"nombreEmpresa\":\"Company " + companySuffix + "\","
                + "\"correoContacto\":\"contact" + companySuffix + "@company.test\","
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

    protected String registerUser(Session admin, String email, String role) throws Exception {
        mockMvc.perform(asJson(admin, post("/api/usuarios"), userData(email, role)))
                .andExpect(status().isCreated());
        return email;
    }

    protected String userData(String email, String role) {
        return "{\"email\":\"" + email + "\","
                + "\"nombre\":\"Carlos\","
                + "\"apellido\":\"Test\","
                + "\"password\":\"" + PASSWORD + "\","
                + "\"rolAcceso\":\"" + role + "\"}";
    }

    protected Session login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();

        Session session = new Session();
        session.email = email;
        session.userId = JsonPath.read(json, "$.usuarioId").toString();
        session.companyId = JsonPath.read(json, "$.empresaId").toString();
        session.role = JsonPath.read(json, "$.rolAcceso");
        session.token = JsonPath.read(json, "$.accessToken");
        session.cookie = result.getResponse().getCookie(REFRESH_COOKIE);
        return session;
    }

    protected String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    protected String createProcess(Session session, String name) throws Exception {
        String body = "{\"nombre\":\"" + name + "\","
                + "\"descripcion\":\"Test process\","
                + "\"categoria\":\"Testing\"}";
        return id(mockMvc.perform(asJson(session, post("/api/procesos"), body))
                .andExpect(status().isCreated())
                .andReturn());
    }

    // El proceso nace con el pool de la empresa propietaria
    protected String ownerPoolId(Session session, String processId) throws Exception {
        MvcResult result = mockMvc.perform(as(session, get("/api/procesos/" + processId)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.poolPropietarioId").toString();
    }

    protected String createProcessRole(Session admin, String name) throws Exception {
        String body = "{\"nombre\":\"" + name + "\",\"descripcion\":\"Rol de prueba\"}";
        return id(mockMvc.perform(asJson(admin, post("/api/roles-proceso"), body))
                .andExpect(status().isCreated())
                .andReturn());
    }

    protected String createLane(Session session, String processId, String poolId, String roleId)
            throws Exception {
        return id(mockMvc.perform(asJson(session,
                        post("/api/procesos/" + processId + "/pools/" + poolId + "/lanes"),
                        "{\"rolProcesoId\":" + roleId + "}"))
                .andExpect(status().isCreated())
                .andReturn());
    }

    protected String activityData(String name, String laneId) {
        return "{\"nombre\":\"" + name + "\","
                + "\"tipoActividad\":\"USUARIO\","
                + "\"posicionX\":100,\"posicionY\":80,"
                + "\"laneId\":" + laneId + "}";
    }

    protected String createActivity(Session session, String processId, String name, String laneId)
            throws Exception {
        return id(mockMvc.perform(asJson(session,
                        post("/api/procesos/" + processId + "/actividades"),
                        activityData(name, laneId)))
                .andExpect(status().isCreated())
                .andReturn());
    }

    protected void shareProcess(Session owner, String processId, Session invited) throws Exception {
        mockMvc.perform(asJson(owner, post("/api/procesos/" + processId + "/compartidos"),
                        "{\"empresaInvitadaId\":" + invited.companyId + "}"))
                .andExpect(status().isCreated());
    }

    protected String id(MvcResult result) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
    }
}
