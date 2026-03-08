package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Staff response")
public record StaffResponse(
    Long id,
    String nome,
    Boolean ativo,
    Instant createdAt,
    List<HorarioFuncionamentoResponse> horarios
) {}
