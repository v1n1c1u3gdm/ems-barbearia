package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Agendamento response")
public record AgendamentoResponse(
    Long id,
    Long clienteId,
    String clienteNome,
    Long servicoId,
    String servicoTitulo,
    Long staffId,
    String staffNome,
    Instant dataHora,
    Instant dataHoraFim,
    String tipo,
    String status,
    Instant createdAt
) {}
