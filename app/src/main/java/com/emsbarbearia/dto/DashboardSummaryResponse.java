package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Dashboard summary with entity counts")
public record DashboardSummaryResponse(

    @Schema(description = "Total relacionamentos")
    long relacionamentos,

    @Schema(description = "Total clientes")
    long clientes,

    @Schema(description = "Total agendamentos")
    long agendamentos,

    @Schema(description = "Total servicos")
    long servicos,

    @Schema(description = "Timestamp of the most recent creation across all entities, null if no data")
    Instant ultimaAtualizacao
) {}
