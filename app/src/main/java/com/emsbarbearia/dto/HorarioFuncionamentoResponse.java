package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "Horário de funcionamento por dia da semana (0=domingo .. 6=sábado)")
public record HorarioFuncionamentoResponse(
    Integer diaSemana,
    Boolean aberto,
    LocalTime horaInicio,
    LocalTime horaFim
) {}
