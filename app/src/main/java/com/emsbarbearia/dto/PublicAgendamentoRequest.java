package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Schema(description = "Public booking request (cliente from JWT)")
public record PublicAgendamentoRequest(
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
    String tipo
) {}
