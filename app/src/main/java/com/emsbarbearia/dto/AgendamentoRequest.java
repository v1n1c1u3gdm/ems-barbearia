package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request for creating or updating an agendamento")
public record AgendamentoRequest(

    @NotNull
    @Schema(description = "Cliente id", requiredMode = Schema.RequiredMode.REQUIRED)
    Long clienteId,

    @NotNull
    @Schema(description = "Appointment date and time", requiredMode = Schema.RequiredMode.REQUIRED)
    Instant dataHora,

    @Size(max = 255)
    @Schema(description = "Service description")
    String servico,

    @NotNull
    @Size(max = 50)
    @Schema(description = "Status (e.g. PENDENTE, CONFIRMADO, CANCELADO, REALIZADO)", requiredMode = Schema.RequiredMode.REQUIRED)
    String status
) {}
