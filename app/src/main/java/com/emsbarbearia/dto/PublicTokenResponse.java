package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token for public cliente")
public record PublicTokenResponse(
    @Schema(description = "JWT bearer token")
    String token
) {}
