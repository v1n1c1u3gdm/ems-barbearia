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

import com.emsbarbearia.dto.ContatoResponse;
import com.emsbarbearia.service.ContatoService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContatoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContatoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ContatoService service;

    @Test
    void list_shouldReturn200AndAllWhenNoFilter() throws Exception {
        ContatoResponse r = new ContatoResponse(1L, "Foo", "foo@x.com", null, "msg", Instant.now());
        when(service.list(null)).thenReturn(List.of(r));
        mockMvc.perform(get("/admin/contatos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nome").value("Foo"));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        ContatoResponse r = new ContatoResponse(1L, "Foo", "foo@x.com", null, "msg", Instant.now());
        when(service.getById(1L)).thenReturn(Optional.of(r));
        mockMvc.perform(get("/admin/contatos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/contatos/999")).andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithBody() throws Exception {
        ContatoResponse r = new ContatoResponse(1L, "New", "new@x.com", null, "hi", Instant.now());
        when(service.create(any())).thenReturn(r);
        mockMvc.perform(post("/admin/contatos")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"New\",\"email\":\"new@x.com\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn200WhenFound() throws Exception {
        ContatoResponse r = new ContatoResponse(1L, "Up", "up@x.com", null, null, Instant.now());
        when(service.update(eq(1L), any())).thenReturn(Optional.of(r));
        mockMvc.perform(put("/admin/contatos/1")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"Updated\",\"email\":\"up@x.com\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        when(service.delete(1L)).thenReturn(true);
        mockMvc.perform(delete("/admin/contatos/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }
}
