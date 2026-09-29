package com.ats.userservice.api.mapper;

import com.ats.userservice.api.dto.response.SsoConfigurationResponse;
import com.ats.userservice.domain.model.sso.aggregate.SsoConfiguration;
import org.springframework.stereotype.Component;

@Component
public class SsoConfigurationResponseMapper {

    public SsoConfigurationResponse toResponse(SsoConfiguration configuration) {
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
