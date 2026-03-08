package com.emsbarbearia.controller;

import com.emsbarbearia.dto.ConfiguracaoAgendaResponse;
import com.emsbarbearia.service.ConfiguracaoAgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/configuracao-agenda")
@Tag(name = "Admin - Configuração Agenda", description = "Configuração de slot e horário de funcionamento para o calendário")
public class ConfiguracaoAgendaController {

    private final ConfiguracaoAgendaService service;

    public ConfiguracaoAgendaController(ConfiguracaoAgendaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get configuração da agenda (slot e horários por dia)")
    public ConfiguracaoAgendaResponse get() {
        return service.getConfig();
    }
}
