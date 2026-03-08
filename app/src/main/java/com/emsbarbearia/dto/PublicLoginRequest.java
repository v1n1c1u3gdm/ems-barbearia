package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Public login: email, senha")
public record PublicLoginRequest(
    @NotBlank
    @Schema(description = "Cliente email", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,
    @NotBlank
    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED)
    String senha
) {}
