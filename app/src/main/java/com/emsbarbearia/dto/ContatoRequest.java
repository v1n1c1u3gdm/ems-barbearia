package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating or updating a contato")
public record ContatoRequest(

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Contact name", requiredMode = Schema.RequiredMode.REQUIRED)
    String nome,

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(description = "Contact email", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @Size(max = 50)
    @Schema(description = "Contact phone")
    String telefone,

    @Schema(description = "Contact message")
    String mensagem
) {}
