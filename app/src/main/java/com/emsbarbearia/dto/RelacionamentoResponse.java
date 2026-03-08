package com.emsbarbearia.dto;

import com.emsbarbearia.entity.CanalRelacionamento;
import com.emsbarbearia.entity.StatusRelacionamento;
import com.emsbarbearia.entity.TipoInteracao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Relacionamento response")
public record RelacionamentoResponse(
    @Schema(description = "Unique identifier")
    Long id,
    @Schema(description = "Name")
    String nome,
    @Schema(description = "Email")
    String email,
    @Schema(description = "Phone")
    String telefone,
    @Schema(description = "Channel")
    CanalRelacionamento canal,
    @Schema(description = "Status")
    StatusRelacionamento status,
    @Schema(description = "Last interaction date")
    Instant dataUltimaInteracao,
    @Schema(description = "Interaction type")
    TipoInteracao tipoInteracao,
    @Schema(description = "Cliente id when linked")
    Long clienteId,
    @Schema(description = "Creation timestamp")
    Instant createdAt
) {}
