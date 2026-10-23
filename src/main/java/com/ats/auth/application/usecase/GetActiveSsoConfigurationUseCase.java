package com.ats.auth.application.usecase;

import com.ats.auth.application.response.SsoConfigurationResponse;
import com.ats.auth.domain.enums.SsoProviderType;
import com.ats.auth.domain.repository.SsoConfigurationRepository;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetActiveSsoConfigurationUseCase {

    private final SsoConfigurationRepository ssoConfigurationRepository;

    public GetActiveSsoConfigurationUseCase(SsoConfigurationRepository ssoConfigurationRepository) {
        this.ssoConfigurationRepository = ssoConfigurationRepository;
    }

    @Transactional(readOnly = true)
    public SsoConfigurationResponse execute(SsoProviderType providerType) {
        return ssoConfigurationRepository.findActiveByProviderType(providerType)
                .map(SsoConfigurationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active " + providerType + " SSO configuration not found"));
    }
}
