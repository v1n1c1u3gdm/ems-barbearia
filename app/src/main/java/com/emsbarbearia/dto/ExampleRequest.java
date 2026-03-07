package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating or updating an example entity")
public record ExampleRequest(

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    @Schema(description = "Entity name", example = "Example", requiredMode = Schema.RequiredMode.REQUIRED)
    String name
) {}
