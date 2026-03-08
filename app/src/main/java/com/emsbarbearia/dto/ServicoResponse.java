package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Servico response")
public record ServicoResponse(
    Long id,
    String titulo,
    String descricao,
    LocalDate validoDe,
    LocalDate validoAte,
    Boolean ativo,
    Integer duracaoMinutos,
    Instant createdAt
) {}
