package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Verify OTP: phone and code")
public record VerifyOtpRequest(
    @NotBlank
    @Size(min = 10, max = 20)
    @Schema(description = "Phone number (same as request)", requiredMode = Schema.RequiredMode.REQUIRED)
    String telefone,
    @NotBlank
    @Pattern(regexp = "^[0-9]{4,8}$", message = "Code must be 4-8 digits")
    @Schema(description = "OTP code received", requiredMode = Schema.RequiredMode.REQUIRED)
    String code
) {}
