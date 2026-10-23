package com.ats.auth.application.usecase;

import com.ats.auth.application.service.SsoConfigurationLoader;
import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.repository.SsoConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Soft delete. */
@Service
public class DeleteSsoConfigurationUseCase {

    private final SsoConfigurationRepository ssoConfigurationRepository;
    private final SsoConfigurationLoader ssoConfigurationLoader;

    public DeleteSsoConfigurationUseCase(SsoConfigurationRepository ssoConfigurationRepository,
                                         SsoConfigurationLoader ssoConfigurationLoader) {
        this.ssoConfigurationRepository = ssoConfigurationRepository;
        this.ssoConfigurationLoader = ssoConfigurationLoader;
    }

    @Transactional
    public void execute(Long id, String actor) {
        SsoConfiguration configuration = ssoConfigurationLoader.load(id);
        configuration.markDeleted(actor);
        ssoConfigurationRepository.save(configuration);
    }
}
