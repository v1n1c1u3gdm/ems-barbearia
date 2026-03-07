package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Example entity response")
public record ExampleResponse(

    @Schema(description = "Unique identifier")
    Long id,

    @Schema(description = "Entity name")
    String name,

    @Schema(description = "Creation timestamp")
    Instant createdAt
) {}
