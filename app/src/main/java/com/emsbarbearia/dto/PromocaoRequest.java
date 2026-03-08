package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request for creating or updating a promocao")
public record PromocaoRequest(
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Promotion title", requiredMode = Schema.RequiredMode.REQUIRED)
    String titulo,
    @Schema(description = "Promotion description")
    String descricao,
    @Schema(description = "Valid from date")
    LocalDate validoDe,
    @Schema(description = "Valid until date")
    LocalDate validoAte,
    @Schema(description = "Whether the promotion is active")
    Boolean ativo
) {}
