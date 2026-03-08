package com.emsbarbearia.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.emsbarbearia.dto.DashboardSummaryResponse;
import com.emsbarbearia.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DashboardService service;

    @Test
    void getSummary_shouldReturn200WithCounts() throws Exception {
        DashboardSummaryResponse response = new DashboardSummaryResponse(10L, 5L, 3L, 2L);
        when(service.getSummary()).thenReturn(response);

        mockMvc.perform(get("/admin/dashboard/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contatos").value(10))
            .andExpect(jsonPath("$.clientes").value(5))
            .andExpect(jsonPath("$.agendamentos").value(3))
            .andExpect(jsonPath("$.promocoes").value(2));
    }
}
