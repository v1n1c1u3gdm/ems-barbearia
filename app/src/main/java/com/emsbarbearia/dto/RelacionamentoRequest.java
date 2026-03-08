package com.emsbarbearia.dto;

import com.emsbarbearia.entity.CanalRelacionamento;
import com.emsbarbearia.entity.StatusRelacionamento;
import com.emsbarbearia.entity.TipoInteracao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request for creating a relacionamento")
public record RelacionamentoRequest(
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Name", requiredMode = Schema.RequiredMode.REQUIRED)
    String nome,
    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(description = "Email", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,
    @Size(max = 50)
    @Schema(description = "Phone")
    String telefone,
    @NotNull
    @Schema(description = "Channel", requiredMode = Schema.RequiredMode.REQUIRED)
    CanalRelacionamento canal,
    @NotNull
    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED)
    StatusRelacionamento status,
    @Schema(description = "Last interaction date")
    Instant dataUltimaInteracao,
    @NotNull
    @Schema(description = "Interaction type", requiredMode = Schema.RequiredMode.REQUIRED)
    TipoInteracao tipoInteracao,
    @Schema(description = "Cliente id when linked")
    Long clienteId
) {}
