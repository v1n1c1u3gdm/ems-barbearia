package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Request for creating or updating an assinatura")
public record AssinaturaRequest(
    @NotNull
    @Schema(description = "Cliente id", requiredMode = Schema.RequiredMode.REQUIRED)
    Long clienteId,
    @NotNull
    @Schema(description = "List of servico ids included in the assinatura", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> servicoIds
) {}
