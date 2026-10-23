package com.ats.auth.application.command;

import com.ats.auth.domain.enums.SsoProviderType;

import java.util.Set;

public record CreateSsoConfigurationCommand(
        SsoProviderType providerType,
        String clientId,
        String clientSecretRef,
        String issuerUri,
        String redirectUri,
        Set<String> scopes
) {
}
