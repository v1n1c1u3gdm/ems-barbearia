package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Cliente response")
public record ClienteResponse(

    @Schema(description = "Unique identifier")
    Long id,

    @Schema(description = "Cliente name")
    String nome,

    @Schema(description = "Cliente email")
    String email,

    @Schema(description = "Cliente phone")
    String telefone,

    @Schema(description = "Creation timestamp")
    Instant createdAt
) {}
