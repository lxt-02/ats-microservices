package com.ats.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        @Size(max = 256, message = "Refresh token must not exceed 256 characters")
        String refreshToken
) {
}
