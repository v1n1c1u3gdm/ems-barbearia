package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dashboard summary with entity counts")
public record DashboardSummaryResponse(

    @Schema(description = "Total contatos")
    long contatos,

    @Schema(description = "Total clientes")
    long clientes,

    @Schema(description = "Total agendamentos")
    long agendamentos,

    @Schema(description = "Total promocoes")
    long promocoes
) {}
