package com.emsbarbearia.dto;

import com.emsbarbearia.entity.StatusRelacionamento;
import com.emsbarbearia.entity.TipoInteracao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Request for updating relacionamento (status, dataUltimaInteracao, tipoInteracao)")
public record RelacionamentoUpdateRequest(
    @Schema(description = "Status")
    StatusRelacionamento status,
    @Schema(description = "Last interaction date")
    Instant dataUltimaInteracao,
    @Schema(description = "Interaction type")
    TipoInteracao tipoInteracao
) {}
