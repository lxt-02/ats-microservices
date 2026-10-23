package com.ats.auth.application.usecase;

import com.ats.auth.application.command.CreateSsoConfigurationCommand;
import com.ats.auth.application.response.SsoConfigurationResponse;
import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.enums.SsoProviderType;
import com.ats.auth.domain.repository.SsoConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSsoConfigurationUseCase {

    private final SsoConfigurationRepository ssoConfigurationRepository;

    public CreateSsoConfigurationUseCase(SsoConfigurationRepository ssoConfigurationRepository) {
        this.ssoConfigurationRepository = ssoConfigurationRepository;
    }

    @Transactional
    public SsoConfigurationResponse execute(CreateSsoConfigurationCommand command, String actor) {
        SsoProviderType providerType = command.providerType() == null ? SsoProviderType.GOOGLE : command.providerType();
        SsoConfiguration configuration = switch (providerType) {
            case KEYCLOAK -> SsoConfiguration.createKeycloak(command.clientId(), command.clientSecretRef(),
                    command.issuerUri(), command.redirectUri(), command.scopes(), actor);
            case GOOGLE -> SsoConfiguration.createGoogle(command.clientId(), command.clientSecretRef(),
                    command.issuerUri(), command.redirectUri(), command.scopes(), actor);
        };
        return SsoConfigurationResponse.from(ssoConfigurationRepository.save(configuration));
    }
}
