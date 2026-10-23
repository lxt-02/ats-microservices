package com.ats.auth.api.request;

import com.ats.auth.application.command.GoogleCodeLoginCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleAuthorizationCodeLoginRequest(
        @NotBlank(message = "Authorization code is required")
        @Size(max = 2048, message = "Authorization code must not exceed 2048 characters")
        String code,
        @Size(max = 500, message = "Redirect URI must not exceed 500 characters")
        String redirectUri,
        /* PKCE verifier, when the frontend started the flow with a code_challenge. */
        @Size(min = 43, max = 128, message = "Code verifier must be 43-128 characters")
        String codeVerifier
) {

    public GoogleCodeLoginCommand toCommand() {
        return new GoogleCodeLoginCommand(code, codeVerifier, redirectUri);
    }
}
