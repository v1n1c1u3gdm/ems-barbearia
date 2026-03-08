package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Configuração da agenda (slot global e horários por dia)")
public record ConfiguracaoAgendaResponse(
    Integer slotMinutos,
    List<HorarioFuncionamentoResponse> horarios
) {}
