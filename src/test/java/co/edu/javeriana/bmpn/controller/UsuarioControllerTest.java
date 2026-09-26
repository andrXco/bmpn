package co.edu.javeriana.bmpn.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.javeriana.bmpn.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void listarDelegaElUsuarioSolicitante() throws Exception {
        when(usuarioService.listarActivos(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios").param("usuarioId", "7"))
                .andExpect(status().isOk());

        verify(usuarioService).listarActivos(7L);
    }

    @Test
    void listarSinUsuarioSolicitanteRespondeSolicitudInvalida() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isBadRequest());
    }
}
