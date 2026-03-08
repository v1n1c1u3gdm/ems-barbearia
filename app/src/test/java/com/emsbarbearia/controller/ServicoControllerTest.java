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

import com.emsbarbearia.dto.ServicoResponse;
import com.emsbarbearia.service.ServicoService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServicoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ServicoService service;

    @Test
    void list_shouldReturn200AndAllWhenNoFilter() throws Exception {
        ServicoResponse response = new ServicoResponse(1L, "Corte", null, null, null, true, 30, Instant.now());
        when(service.list(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/admin/servicos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].titulo").value("Corte"));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        ServicoResponse response = new ServicoResponse(1L, "Corte", "desc", null, null, true, 45, Instant.now());
        when(service.getById(1L)).thenReturn(Optional.of(response));
        mockMvc.perform(get("/admin/servicos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/servicos/999")).andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithBody() throws Exception {
        ServicoResponse response = new ServicoResponse(1L, "New", null, null, null, true, 30, Instant.now());
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/admin/servicos")
                .contentType(APPLICATION_JSON)
                .content("{\"titulo\":\"New\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(service.update(eq(999L), any())).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/servicos/999")
                .contentType(APPLICATION_JSON)
                .content("{\"titulo\":\"Any\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        when(service.delete(1L)).thenReturn(true);
        mockMvc.perform(delete("/admin/servicos/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }
}
