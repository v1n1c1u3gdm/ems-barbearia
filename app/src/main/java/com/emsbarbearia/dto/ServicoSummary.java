package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Servico summary in assinatura")
public record ServicoSummary(Long id, String titulo) {}
