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

import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.service.AgendamentoService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AgendamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AgendamentoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AgendamentoService service;

    @Test
    void list_shouldReturn200AndAllWhenNoFilter() throws Exception {
        var response = new AgendamentoResponse(1L, 10L, "Cliente A", Instant.now(), "Corte", "PENDENTE", Instant.now());
        when(service.list(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/admin/agendamentos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].clienteNome").value("Cliente A"));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        var response = new AgendamentoResponse(1L, 10L, "Cliente", Instant.now(), null, "CONFIRMADO", Instant.now());
        when(service.getById(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/admin/agendamentos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/agendamentos/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WhenClienteExists() throws Exception {
        var response = new AgendamentoResponse(1L, 10L, "C", Instant.now(), "Corte", "PENDENTE", Instant.now());
        when(service.create(any())).thenReturn(Optional.of(response));

        mockMvc.perform(post("/admin/agendamentos")
                .contentType(APPLICATION_JSON)
                .content("{\"clienteId\":10,\"dataHora\":\"2025-06-01T10:00:00Z\",\"status\":\"PENDENTE\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_shouldReturn404WhenClienteNotFound() throws Exception {
        when(service.create(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/agendamentos")
                .contentType(APPLICATION_JSON)
                .content("{\"clienteId\":999,\"dataHora\":\"2025-06-01T10:00:00Z\",\"status\":\"PENDENTE\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(service.update(eq(999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/admin/agendamentos/999")
                .contentType(APPLICATION_JSON)
                .content("{\"clienteId\":1,\"dataHora\":\"2025-06-01T10:00:00Z\",\"status\":\"PENDENTE\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        when(service.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/admin/agendamentos/1"))
            .andExpect(status().isNoContent());
        verify(service).delete(1L);
    }
}
