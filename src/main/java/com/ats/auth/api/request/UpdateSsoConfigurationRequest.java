package com.ats.auth.api.request;

import com.ats.auth.application.command.UpdateSsoConfigurationCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateSsoConfigurationRequest(
        @NotBlank(message = "Client id is required")
        @Size(max = 500, message = "Client id must not exceed 500 characters")
        String clientId,
        @Size(max = 500, message = "Client secret reference must not exceed 500 characters")
        String clientSecretRef,
        @NotBlank(message = "Issuer URI is required")
        @Size(max = 500, message = "Issuer URI must not exceed 500 characters")
        String issuerUri,
        @Size(max = 500, message = "Redirect URI must not exceed 500 characters")
        String redirectUri,
        Set<String> scopes
) {

    public UpdateSsoConfigurationCommand toCommand(Long id) {
        return new UpdateSsoConfigurationCommand(id, clientId, clientSecretRef, issuerUri, redirectUri, scopes);
    }
}
