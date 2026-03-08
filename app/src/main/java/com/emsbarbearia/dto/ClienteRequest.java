package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating or updating a cliente")
public record ClienteRequest(

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Cliente name", requiredMode = Schema.RequiredMode.REQUIRED)
    String nome,

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(description = "Cliente email", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @Size(max = 50)
    @Schema(description = "Cliente phone")
    String telefone
) {}
