package com.ats.auth.application.usecase;

import com.ats.auth.application.command.UpdateSsoConfigurationCommand;
import com.ats.auth.application.response.SsoConfigurationResponse;
import com.ats.auth.application.service.SsoConfigurationLoader;
import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.repository.SsoConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateSsoConfigurationUseCase {

    private final SsoConfigurationRepository ssoConfigurationRepository;
    private final SsoConfigurationLoader ssoConfigurationLoader;

    public UpdateSsoConfigurationUseCase(SsoConfigurationRepository ssoConfigurationRepository,
                                         SsoConfigurationLoader ssoConfigurationLoader) {
        this.ssoConfigurationRepository = ssoConfigurationRepository;
        this.ssoConfigurationLoader = ssoConfigurationLoader;
    }

    @Transactional
    public SsoConfigurationResponse execute(UpdateSsoConfigurationCommand command, String actor) {
        SsoConfiguration configuration = ssoConfigurationLoader.load(command.id());
        configuration.updateClient(command.clientId(), command.clientSecretRef(), actor);
        configuration.updateIssuer(command.issuerUri(), actor);
        configuration.updateRedirectAndScopes(command.redirectUri(), command.scopes(), actor);
        return SsoConfigurationResponse.from(ssoConfigurationRepository.save(configuration));
    }
}
