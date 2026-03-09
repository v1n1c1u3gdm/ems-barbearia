package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Public slot view: no client data, for day agenda")
public record PublicSlotResponse(
    Long staffId,
    String staffNome,
    Instant dataHora,
    Instant dataHoraFim,
    String tipo,
    String status
) {}
