package com.ats.userservice.application.command.sso;

import com.ats.userservice.domain.model.sso.enums.SsoProviderType;

import java.util.Set;

public record CreateSsoConfigurationCommand(
        SsoProviderType providerType,
        String clientId,
        String clientSecretRef,
        String issuerUri,
        String redirectUri,
        Set<String> scopes,
        String createdBy
) {
}
