package com.ats.auth.application.command;

import java.util.Set;

public record UpdateSsoConfigurationCommand(
        Long id,
        String clientId,
        String clientSecretRef,
        String issuerUri,
        String redirectUri,
        Set<String> scopes
) {
}
