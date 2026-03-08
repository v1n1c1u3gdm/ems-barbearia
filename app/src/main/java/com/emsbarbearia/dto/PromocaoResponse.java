package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Promocao response")
public record PromocaoResponse(

    @Schema(description = "Unique identifier")
    Long id,

    @Schema(description = "Promotion title")
    String titulo,

    @Schema(description = "Promotion description")
    String descricao,

    @Schema(description = "Valid from date")
    LocalDate validoDe,

    @Schema(description = "Valid until date")
    LocalDate validoAte,

    @Schema(description = "Whether the promotion is active")
    Boolean ativo,

    @Schema(description = "Creation timestamp")
    Instant createdAt
) {}
