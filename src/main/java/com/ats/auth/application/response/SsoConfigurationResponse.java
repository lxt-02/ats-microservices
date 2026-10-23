package com.ats.auth.application.response;

import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.enums.SsoProviderType;

import java.time.Instant;
import java.util.Set;

public record SsoConfigurationResponse(
        Long id,
        SsoProviderType providerType,
        String clientId,
        String clientSecretRef,
        String issuerUri,
        String redirectUri,
        Set<String> scopes,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {

    public static SsoConfigurationResponse from(SsoConfiguration configuration) {
        return new SsoConfigurationResponse(
                configuration.getId(),
                configuration.getProviderType(),
                configuration.getClientId(),
                configuration.getClientSecretRef(),
                configuration.getIssuerUri(),
                configuration.getRedirectUri(),
                configuration.getScopes(),
                configuration.isActive(),
                configuration.getCreatedAt(),
                configuration.getUpdatedAt(),
                configuration.getCreatedBy(),
                configuration.getUpdatedBy()
        );
    }
}
