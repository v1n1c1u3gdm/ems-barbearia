package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "Horário de funcionamento por dia (0=domingo .. 6=sábado) para request")
public record HorarioFuncionamentoRequest(
    Integer diaSemana,
    Boolean aberto,
    LocalTime horaInicio,
    LocalTime horaFim
) {}
