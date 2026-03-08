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

import com.emsbarbearia.dto.StaffResponse;
import com.emsbarbearia.service.StaffService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StaffController.class)
@AutoConfigureMockMvc(addFilters = false)
class StaffControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    StaffService service;

    @Test
    void list_shouldReturn200AndAllWhenNoFilter() throws Exception {
        StaffResponse response = new StaffResponse(1L, "João", true, Instant.now(), List.of());
        when(service.list(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/admin/staff"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nome").value("João"));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        StaffResponse response = new StaffResponse(1L, "João", true, Instant.now(), List.of());
        when(service.getById(1L)).thenReturn(Optional.of(response));
        mockMvc.perform(get("/admin/staff/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("João"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/staff/999")).andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithBody() throws Exception {
        StaffResponse response = new StaffResponse(1L, "Maria", true, Instant.now(), List.of());
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/admin/staff")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"Maria\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("Maria"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(service.update(eq(999L), any())).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/staff/999")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"Any\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        when(service.delete(1L)).thenReturn(true);
        mockMvc.perform(delete("/admin/staff/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotExists() throws Exception {
        when(service.delete(999L)).thenReturn(false);
        mockMvc.perform(delete("/admin/staff/999")).andExpect(status().isNotFound());
    }
}
