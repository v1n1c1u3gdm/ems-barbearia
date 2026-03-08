package com.emsbarbearia.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.emsbarbearia.dto.ConfiguracaoAgendaResponse;
import com.emsbarbearia.dto.HorarioFuncionamentoResponse;
import com.emsbarbearia.service.ConfiguracaoAgendaService;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ConfiguracaoAgendaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConfiguracaoAgendaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ConfiguracaoAgendaService service;

    @Test
    void get_shouldReturn200WithSlotAndHorarios() throws Exception {
        var horarios = List.of(
            new HorarioFuncionamentoResponse(0, false, null, null),
            new HorarioFuncionamentoResponse(2, true, LocalTime.of(9, 0), LocalTime.of(19, 0))
        );
        var response = new ConfiguracaoAgendaResponse(30, horarios);
        when(service.getConfig()).thenReturn(response);

        mockMvc.perform(get("/admin/configuracao-agenda"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.slotMinutos").value(30))
            .andExpect(jsonPath("$.horarios.length()").value(2))
            .andExpect(jsonPath("$.horarios[0].diaSemana").value(0))
            .andExpect(jsonPath("$.horarios[0].aberto").value(false))
            .andExpect(jsonPath("$.horarios[1].diaSemana").value(2))
            .andExpect(jsonPath("$.horarios[1].aberto").value(true))
            .andExpect(jsonPath("$.horarios[1].horaInicio").value("09:00:00"))
            .andExpect(jsonPath("$.horarios[1].horaFim").value("19:00:00"));
    }
}
