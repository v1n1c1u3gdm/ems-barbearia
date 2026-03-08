package com.emsbarbearia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request OTP: phone in E.164 or national format")
public record PhoneOtpRequest(
    @NotBlank
    @Size(min = 10, max = 20)
    @Pattern(regexp = "^\\+?[0-9]{10,20}$", message = "Invalid phone format")
    @Schema(description = "Phone number (E.164 preferred)", requiredMode = Schema.RequiredMode.REQUIRED)
    String telefone
) {}
