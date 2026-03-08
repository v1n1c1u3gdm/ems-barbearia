package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Public registration: nome, email, senha")
public record PublicRegisterRequest(
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Cliente name", requiredMode = Schema.RequiredMode.REQUIRED)
    String nome,
    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(description = "Cliente email", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,
    @NotBlank
    @Size(min = 6, max = 100)
    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED)
    String senha
) {}
