package co.edu.javeriana.bmpn.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Pruebas de integracion del editor de procesos: elementos del diagrama (lanes, actividades,
// gateways y arcos) con la seguridad activa. Verifican el flujo de modelado, los permisos por rol
// y que una empresa no pueda tocar los elementos de otra, ni siquiera conociendo sus identificadores.
class DiagramaIntegrationTest extends ApiIntegrationTest {

    private String processId;
    private String poolId;
    private String laneId;

    // Proceso de la empresa A con una lane lista para recibir actividades
    @BeforeEach
    void setUpProcess() throws Exception {
        processId = createProcess(adminA, "Compras");
        poolId = ownerPoolId(adminA, processId);
        String roleId = createProcessRole(adminA, "Analista");
        laneId = createLane(adminA, processId, poolId, roleId);
    }

    // ---- Flujo completo del editor ----

    @Test
    void editorModelsADiagramAndItCanBeRead() throws Exception {
        String reviewId = createActivity(editorA, processId, "Revisar solicitud", laneId);
        String approveId = createActivity(editorA, processId, "Aprobar compra", laneId);

        String gatewayId = id(mockMvc.perform(asJson(editorA,
                        post("/api/procesos/" + processId + "/gateways"),
                        "{\"nombre\":\"Monto alto?\",\"tipoGateway\":\"EXCLUSIVO\","
                                + "\"posicionX\":300,\"posicionY\":80}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoGateway").value("EXCLUSIVO"))
                .andReturn());

        mockMvc.perform(asJson(editorA, post("/api/procesos/" + processId + "/arcos"),
                        "{\"origenId\":" + reviewId + ",\"destinoId\":" + gatewayId + "}"))
                .andExpect(status().isCreated());

        // Un gateway exclusivo si puede tener arcos con condicion
        mockMvc.perform(asJson(editorA, post("/api/procesos/" + processId + "/arcos"),
                        "{\"origenId\":" + gatewayId + ",\"destinoId\":" + approveId + ","
                                + "\"etiqueta\":\"Si\",\"condicion\":\"monto > 1000\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.condicion").value("monto > 1000"));

        // El lector consulta el diagrama completo
        mockMvc.perform(as(readerA, get("/api/procesos/" + processId + "/actividades")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(as(readerA, get("/api/procesos/" + processId + "/arcos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(as(readerA, get("/api/procesos/" + processId + "/pools/" + poolId + "/lanes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreRol").value("Analista"));
    }

    @Test
    void editorRenamesAnActivity() throws Exception {
        String activityId = createActivity(editorA, processId, "Revisar", laneId);

        mockMvc.perform(asJson(editorA,
                        put("/api/procesos/" + processId + "/actividades/" + activityId),
                        activityData("Revisar solicitud", laneId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Revisar solicitud"));
    }

    // ---- Reglas de negocio del modelado ----

    @Test
    void duplicatedActivityNameReturns409() throws Exception {
        createActivity(editorA, processId, "Revisar", laneId);

        mockMvc.perform(asJson(editorA, post("/api/procesos/" + processId + "/actividades"),
                        activityData("revisar", laneId)))
                .andExpect(status().isConflict());
    }

    @Test
    void arcFromAnElementToItselfReturns400() throws Exception {
        String activityId = createActivity(editorA, processId, "Revisar", laneId);

        mockMvc.perform(asJson(editorA, post("/api/procesos/" + processId + "/arcos"),
                        "{\"origenId\":" + activityId + ",\"destinoId\":" + activityId + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void conditionOnArcLeavingAnActivityReturns400() throws Exception {
        String first = createActivity(editorA, processId, "Revisar", laneId);
        String second = createActivity(editorA, processId, "Aprobar", laneId);

        mockMvc.perform(asJson(editorA, post("/api/procesos/" + processId + "/arcos"),
                        "{\"origenId\":" + first + ",\"destinoId\":" + second
                                + ",\"condicion\":\"monto > 1000\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activityWithoutRequiredFieldsReturns400() throws Exception {
        mockMvc.perform(asJson(editorA, post("/api/procesos/" + processId + "/actividades"),
                        "{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---- Permisos por rol ----

    @Test
    void readerCannotCreateActivities() throws Exception {
        mockMvc.perform(asJson(readerA, post("/api/procesos/" + processId + "/actividades"),
                        activityData("Revisar", laneId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void readerCannotCreateArcs() throws Exception {
        String first = createActivity(editorA, processId, "Revisar", laneId);
        String second = createActivity(editorA, processId, "Aprobar", laneId);

        mockMvc.perform(asJson(readerA, post("/api/procesos/" + processId + "/arcos"),
                        "{\"origenId\":" + first + ",\"destinoId\":" + second + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdministratorDeletesActivities() throws Exception {
        String activityId = createActivity(editorA, processId, "Revisar", laneId);

        mockMvc.perform(as(editorA, delete("/api/procesos/" + processId + "/actividades/" + activityId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(adminA, delete("/api/procesos/" + processId + "/actividades/" + activityId)))
                .andExpect(status().isOk());

        mockMvc.perform(as(adminA, get("/api/procesos/" + processId + "/actividades")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void editorCannotCreateProcessRoles() throws Exception {
        mockMvc.perform(asJson(editorA, post("/api/roles-proceso"),
                        "{\"nombre\":\"Gerente\",\"descripcion\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    // ---- Aislamiento entre empresas en los subrecursos ----

    @Test
    void otherCompanyCannotReadDiagramElements() throws Exception {
        createActivity(editorA, processId, "Revisar", laneId);

        mockMvc.perform(as(adminB, get("/api/procesos/" + processId + "/actividades")))
                .andExpect(status().isNotFound());
        mockMvc.perform(as(adminB, get("/api/procesos/" + processId + "/arcos")))
                .andExpect(status().isNotFound());
        mockMvc.perform(as(adminB, get("/api/procesos/" + processId + "/pools")))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherCompanyCannotModifyDiagramElements() throws Exception {
        String activityId = createActivity(editorA, processId, "Revisar", laneId);

        mockMvc.perform(asJson(adminB, post("/api/procesos/" + processId + "/actividades"),
                        activityData("Intrusa", laneId)))
                .andExpect(status().isNotFound());
        mockMvc.perform(asJson(adminB,
                        put("/api/procesos/" + processId + "/actividades/" + activityId),
                        activityData("Cambiada por B", laneId)))
                .andExpect(status().isNotFound());
        mockMvc.perform(as(adminB, delete("/api/procesos/" + processId + "/actividades/" + activityId)))
                .andExpect(status().isNotFound());

        // La actividad sigue intacta para la empresa A
        mockMvc.perform(as(adminA, get("/api/procesos/" + processId + "/actividades/" + activityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Revisar"));
    }

    @Test
    void laneCannotUseAProcessRoleOfAnotherCompany() throws Exception {
        String roleOfB = createProcessRole(adminB, "Rol de B");

        mockMvc.perform(asJson(adminA,
                        post("/api/procesos/" + processId + "/pools/" + poolId + "/lanes"),
                        "{\"rolProcesoId\":" + roleOfB + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherCompanyCannotSeeProcessRoles() throws Exception {
        String roleOfA = createProcessRole(adminA, "Auditor");

        mockMvc.perform(as(adminB, get("/api/roles-proceso/" + roleOfA)))
                .andExpect(status().isNotFound());
    }

    // ---- Proceso compartido (HU-23): la invitada lee el diagrama pero no lo modifica ----

    @Test
    void invitedCompanyReadsDiagramButCannotModifyIt() throws Exception {
        createActivity(editorA, processId, "Revisar", laneId);
        shareProcess(adminA, processId, adminB);

        mockMvc.perform(as(adminB, get("/api/procesos/" + processId + "/actividades")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(as(adminB, get("/api/procesos/" + processId + "/pools/" + poolId + "/lanes")))
                .andExpect(status().isOk());

        mockMvc.perform(asJson(adminB, post("/api/procesos/" + processId + "/actividades"),
                        activityData("Intrusa", laneId)))
                .andExpect(status().isNotFound());
    }
}
