package com.ats.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleIdTokenLoginRequest(
        @NotBlank(message = "Google ID token is required")
        @Size(max = 4096, message = "Google ID token must not exceed 4096 characters")
        String idToken
) {
}
