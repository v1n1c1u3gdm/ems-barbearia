package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Assinatura response")
public record AssinaturaResponse(
    Long id,
    Long clienteId,
    String clienteNome,
    List<ServicoSummary> servicos,
    Instant createdAt
) {}
