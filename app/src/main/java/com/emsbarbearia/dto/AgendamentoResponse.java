package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Agendamento response")
public record AgendamentoResponse(

    @Schema(description = "Unique identifier")
    Long id,

    @Schema(description = "Cliente id")
    Long clienteId,

    @Schema(description = "Cliente name")
    String clienteNome,

    @Schema(description = "Appointment date and time")
    Instant dataHora,

    @Schema(description = "Service description")
    String servico,

    @Schema(description = "Status")
    String status,

    @Schema(description = "Creation timestamp")
    Instant createdAt
) {}
