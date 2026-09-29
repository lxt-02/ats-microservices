package com.ats.userservice.application.command.sso;

import java.util.Set;

public record UpdateSsoConfigurationCommand(
        Long id,
        String clientId,
        String clientSecretRef,
        String issuerUri,
        String redirectUri,
        Set<String> scopes,
        String updatedBy
) {
}
