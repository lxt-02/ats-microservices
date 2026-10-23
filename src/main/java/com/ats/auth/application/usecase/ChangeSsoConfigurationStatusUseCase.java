package com.ats.auth.application.usecase;

import com.ats.auth.application.command.ChangeSsoConfigurationStatusCommand;
import com.ats.auth.application.response.SsoConfigurationResponse;
import com.ats.auth.application.service.SsoConfigurationLoader;
import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.repository.SsoConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeSsoConfigurationStatusUseCase {

    private final SsoConfigurationRepository ssoConfigurationRepository;
    private final SsoConfigurationLoader ssoConfigurationLoader;

    public ChangeSsoConfigurationStatusUseCase(SsoConfigurationRepository ssoConfigurationRepository,
                                               SsoConfigurationLoader ssoConfigurationLoader) {
        this.ssoConfigurationRepository = ssoConfigurationRepository;
        this.ssoConfigurationLoader = ssoConfigurationLoader;
    }

    @Transactional
    public SsoConfigurationResponse execute(ChangeSsoConfigurationStatusCommand command, String actor) {
        SsoConfiguration configuration = ssoConfigurationLoader.load(command.id());
        if (command.active()) {
            configuration.activate(actor);
        } else {
            configuration.deactivate(actor);
        }
        return SsoConfigurationResponse.from(ssoConfigurationRepository.save(configuration));
    }
}
