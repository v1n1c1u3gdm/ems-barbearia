package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request for creating or updating staff")
public record StaffRequest(
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Staff name", requiredMode = Schema.RequiredMode.REQUIRED)
    String nome,
    @Schema(description = "Whether the staff is active")
    Boolean ativo,
    @Schema(description = "Weekly availability (0=Sunday .. 6=Saturday); when present replaces all")
    List<HorarioFuncionamentoRequest> horarios
) {}
