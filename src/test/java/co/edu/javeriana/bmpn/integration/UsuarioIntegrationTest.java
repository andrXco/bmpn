package co.edu.javeriana.bmpn.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

// Pruebas de integracion del modulo de usuarios: solo el administrador gestiona colaboradores
// y unicamente los de su propia empresa.
class UsuarioIntegrationTest extends ApiIntegrationTest {

    @Test
    void administratorListsOnlyUsersOfOwnCompany() throws Exception {
        // Empresa A: admin, editor y lector. Empresa B: solo su admin.
        mockMvc.perform(as(adminA, get("/api/usuarios")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(as(adminB, get("/api/usuarios")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void editorAndReaderCannotManageUsers() throws Exception {
        // Consultar el listado de la propia empresa esta permitido a cualquier rol (UsuarioServiceTest)
        mockMvc.perform(as(readerA, get("/api/usuarios")))
                .andExpect(status().isOk());

        mockMvc.perform(asJson(editorA, post("/api/usuarios"),
                        userData("nuevo" + suffix + "@a.test", "ADMINISTRADOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(asJson(readerA, patch("/api/usuarios/" + readerA.userId + "/rol"),
                        "{\"rolAcceso\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicatedEmailReturns409() throws Exception {
        mockMvc.perform(asJson(adminA, post("/api/usuarios"), userData(editorA.email, "EDITOR")))
                .andExpect(status().isConflict());
    }

    @Test
    void roleChangeAppliesOnNextLogin() throws Exception {
        mockMvc.perform(asJson(adminA, patch("/api/usuarios/" + readerA.userId + "/rol"),
                        "{\"rolAcceso\":\"EDITOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolAcceso").value("EDITOR"));

        Session promoted = login(readerA.email);
        mockMvc.perform(asJson(promoted, post("/api/procesos"),
                        "{\"nombre\":\"Ventas\",\"descripcion\":\"x\",\"categoria\":\"y\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void otherCompanyAdministratorCannotChangeRoleOrDeactivate() throws Exception {
        mockMvc.perform(asJson(adminB, patch("/api/usuarios/" + editorA.userId + "/rol"),
                        "{\"rolAcceso\":\"SOLO_LECTURA\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(as(adminB, delete("/api/usuarios/" + editorA.userId)))
                .andExpect(status().isNotFound());

        // El editor de A sigue activo y con su rol
        login(editorA.email);
    }

    @Test
    void deactivatedUserCannotLogIn() throws Exception {
        mockMvc.perform(as(adminA, delete("/api/usuarios/" + editorA.userId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(editorA.email, PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotActAsAnotherUserOfSameCompany() throws Exception {
        // El token es del lector, pero intenta usar el usuarioId del administrador
        mockMvc.perform(get("/api/usuarios")
                        .param("usuarioId", adminA.userId)
                        .header("Authorization", readerA.bearer()))
                .andExpect(status().isForbidden());
    }
}
