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

import com.emsbarbearia.dto.AssinaturaResponse;
import com.emsbarbearia.dto.ServicoSummary;
import com.emsbarbearia.service.AssinaturaService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssinaturaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssinaturaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AssinaturaService service;

    @Test
    void list_shouldReturn200AndAllWhenNoFilter() throws Exception {
        AssinaturaResponse response = new AssinaturaResponse(
            1L, 10L, "Cliente A", List.of(new ServicoSummary(1L, "Corte")), Instant.now());
        when(service.list(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/admin/assinaturas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].clienteNome").value("Cliente A"));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        AssinaturaResponse response = new AssinaturaResponse(
            1L, 10L, "Cliente", List.of(new ServicoSummary(1L, "Corte")), Instant.now());
        when(service.getById(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/admin/assinaturas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/assinaturas/999")).andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WhenClienteExists() throws Exception {
        AssinaturaResponse response = new AssinaturaResponse(
            1L, 10L, "C", List.of(new ServicoSummary(1L, "Corte")), Instant.now());
        when(service.create(any())).thenReturn(Optional.of(response));

        mockMvc.perform(post("/admin/assinaturas")
                .contentType(APPLICATION_JSON)
                .content("{\"clienteId\":10,\"servicoIds\":[1]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_shouldReturn404WhenClienteNotFound() throws Exception {
        when(service.create(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/assinaturas")
                .contentType(APPLICATION_JSON)
                .content("{\"clienteId\":999,\"servicoIds\":[1]}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(service.update(eq(999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/admin/assinaturas/999")
                .contentType(APPLICATION_JSON)
                .content("{\"clienteId\":1,\"servicoIds\":[1]}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        when(service.delete(1L)).thenReturn(true);
        mockMvc.perform(delete("/admin/assinaturas/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }
}
