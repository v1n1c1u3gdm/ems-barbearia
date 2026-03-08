package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Contato response")
public record ContatoResponse(

    @Schema(description = "Unique identifier")
    Long id,

    @Schema(description = "Contact name")
    String nome,

    @Schema(description = "Contact email")
    String email,

    @Schema(description = "Contact phone")
    String telefone,

    @Schema(description = "Contact message")
    String mensagem,

    @Schema(description = "Creation timestamp")
    Instant createdAt
) {}
