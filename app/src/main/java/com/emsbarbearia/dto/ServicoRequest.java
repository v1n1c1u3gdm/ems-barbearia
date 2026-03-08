package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request for creating or updating a servico")
public record ServicoRequest(
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Service title", requiredMode = Schema.RequiredMode.REQUIRED)
    String titulo,
    @Schema(description = "Service description")
    String descricao,
    @Schema(description = "Valid from date")
    LocalDate validoDe,
    @Schema(description = "Valid until date")
    LocalDate validoAte,
    @Schema(description = "Whether the service is active")
    Boolean ativo,
    @Schema(description = "Duration in minutes")
    Integer duracaoMinutos
) {}
