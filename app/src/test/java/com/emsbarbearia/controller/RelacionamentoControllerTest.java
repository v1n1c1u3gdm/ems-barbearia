package com.emsbarbearia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.emsbarbearia.dto.RelacionamentoResponse;
import com.emsbarbearia.entity.CanalRelacionamento;
import com.emsbarbearia.entity.StatusRelacionamento;
import com.emsbarbearia.entity.TipoInteracao;
import com.emsbarbearia.service.RelacionamentoService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RelacionamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class RelacionamentoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RelacionamentoService service;

    @Test
    void list_shouldReturn200AndAllWhenNoFilter() throws Exception {
        RelacionamentoResponse r = response(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE);
        when(service.list(null, null)).thenReturn(List.of(r));
        mockMvc.perform(get("/admin/relacionamentos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].canal").value("EMAIL"))
            .andExpect(jsonPath("$[0].status").value("QUENTE"));
    }

    @Test
    void list_shouldFilterByCanalAndStatus() throws Exception {
        RelacionamentoResponse r = response(1L, CanalRelacionamento.WHATSAPP, StatusRelacionamento.MORNO);
        when(service.list(CanalRelacionamento.WHATSAPP, StatusRelacionamento.MORNO)).thenReturn(List.of(r));
        mockMvc.perform(get("/admin/relacionamentos")
                .param("canal", "WHATSAPP")
                .param("status", "MORNO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].canal").value("WHATSAPP"))
            .andExpect(jsonPath("$[0].status").value("MORNO"));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        RelacionamentoResponse r = response(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE);
        when(service.getById(1L)).thenReturn(Optional.of(r));
        mockMvc.perform(get("/admin/relacionamentos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/relacionamentos/999")).andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithBody() throws Exception {
        RelacionamentoResponse r = response(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE);
        when(service.create(any())).thenReturn(r);
        mockMvc.perform(post("/admin/relacionamentos")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"New\",\"email\":\"new@x.com\",\"canal\":\"EMAIL\",\"status\":\"QUENTE\",\"tipoInteracao\":\"MOTIVADA_PELO_CLIENTE\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn200WhenFound() throws Exception {
        RelacionamentoResponse r = response(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.MORNO);
        when(service.update(eq(1L), any())).thenReturn(Optional.of(r));
        mockMvc.perform(put("/admin/relacionamentos/1")
                .contentType(APPLICATION_JSON)
                .content("{\"status\":\"MORNO\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("MORNO"));
    }

    @Test
    void update_shouldReturn400WhenBodyNull() throws Exception {
        mockMvc.perform(put("/admin/relacionamentos/1")
                .contentType(APPLICATION_JSON)
                .content(""))
            .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        when(service.delete(1L)).thenReturn(true);
        mockMvc.perform(delete("/admin/relacionamentos/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotExists() throws Exception {
        when(service.delete(999L)).thenReturn(false);
        mockMvc.perform(delete("/admin/relacionamentos/999")).andExpect(status().isNotFound());
    }

    private static RelacionamentoResponse response(Long id, CanalRelacionamento canal, StatusRelacionamento status) {
        return new RelacionamentoResponse(
            id, "Foo", "foo@x.com", null,
            canal, status, null, TipoInteracao.MOTIVADA_PELO_CLIENTE, null, Instant.now());
    }
}
