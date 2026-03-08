package com.emsbarbearia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.dto.ServicoResponse;
import com.emsbarbearia.dto.StaffResponse;
import com.emsbarbearia.service.AgendamentoService;
import com.emsbarbearia.service.ServicoService;
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

@WebMvcTest(PublicApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ServicoService servicoService;

    @MockBean
    StaffService staffService;

    @MockBean
    AgendamentoService agendamentoService;

    @Test
    void listServicos_shouldReturn200AndActiveServicos() throws Exception {
        ServicoResponse response = new ServicoResponse(1L, "Corte", null, null, null, true, 30, Instant.now());
        when(servicoService.listAtivos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/servicos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].titulo").value("Corte"));
    }

    @Test
    void listStaff_shouldReturn200AndActiveStaff() throws Exception {
        StaffResponse response = new StaffResponse(1L, "João", true, Instant.now(), List.of());
        when(staffService.listAtivos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/staff"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("João"));
    }

    @Test
    void createAgendamento_shouldReturn201WhenCreated() throws Exception {
        AgendamentoResponse response = new AgendamentoResponse(
            1L, 10L, "Cliente", 1L, "Corte", 1L, "João",
            Instant.now(), null, "FIRME", "PENDENTE", Instant.now());
        when(agendamentoService.create(any())).thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/agendamentos")
                .contentType(APPLICATION_JSON)
                .content("{\"clienteId\":10,\"servicoId\":1,\"staffId\":1,\"dataHora\":\"2025-06-01T10:00:00Z\",\"tipo\":\"FIRME\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void createAgendamento_shouldReturn404WhenClienteOrServicoOrStaffNotFound() throws Exception {
        when(agendamentoService.create(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/agendamentos")
                .contentType(APPLICATION_JSON)
                .content("{\"clienteId\":999,\"servicoId\":1,\"staffId\":1,\"dataHora\":\"2025-06-01T10:00:00Z\",\"tipo\":\"FIRME\"}"))
            .andExpect(status().isNotFound());
    }
}
