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
    @Schema(description = "Servico id", requiredMode = Schema.RequiredMode.REQUIRED)
    Long servicoId,
    @NotNull
    @Schema(description = "Staff id", requiredMode = Schema.RequiredMode.REQUIRED)
    Long staffId,
    @NotNull
    @Schema(description = "Appointment date and time", requiredMode = Schema.RequiredMode.REQUIRED)
    Instant dataHora,
    @NotNull
    @Size(max = 20)
    @Schema(description = "Tipo: FIRME or ENCAIXE", requiredMode = Schema.RequiredMode.REQUIRED)
    String tipo,
    @Size(max = 50)
    @Schema(description = "Status (e.g. PENDENTE, APROVADO, CANCELADO, REALIZADO); optional on create, default PENDENTE")
    String status
) {}
