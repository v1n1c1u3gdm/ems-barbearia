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

import com.emsbarbearia.dto.ClienteResponse;
import com.emsbarbearia.service.ClienteService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ClienteService service;

    @Test
    void list_shouldReturn200AndAllWhenNoFilter() throws Exception {
        ClienteResponse response = new ClienteResponse(1L, "Foo", "foo@x.com", null, Instant.now());
        when(service.list(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/admin/clientes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nome").value("Foo"));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        ClienteResponse response = new ClienteResponse(1L, "Foo", "foo@x.com", "11999999999", Instant.now());
        when(service.getById(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/admin/clientes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("Foo"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/clientes/999")).andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithBody() throws Exception {
        ClienteResponse response = new ClienteResponse(1L, "New", "new@x.com", null, Instant.now());
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/admin/clientes")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"New\",\"email\":\"new@x.com\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("New"));
    }

    @Test
    void update_shouldReturn200WhenFound() throws Exception {
        ClienteResponse response = new ClienteResponse(1L, "Updated", "up@x.com", null, Instant.now());
        when(service.update(eq(1L), any())).thenReturn(Optional.of(response));

        mockMvc.perform(put("/admin/clientes/1")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"Updated\",\"email\":\"up@x.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Updated"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(service.update(eq(999L), any())).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/clientes/999")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"Any\",\"email\":\"a@x.com\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        when(service.delete(1L)).thenReturn(true);
        mockMvc.perform(delete("/admin/clientes/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotExists() throws Exception {
        when(service.delete(999L)).thenReturn(false);
        mockMvc.perform(delete("/admin/clientes/999")).andExpect(status().isNotFound());
        verify(service).delete(999L);
    }
}
