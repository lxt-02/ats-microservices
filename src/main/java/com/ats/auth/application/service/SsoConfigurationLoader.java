package com.ats.auth.application.service;

import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.repository.SsoConfigurationRepository;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SsoConfigurationLoader {

    private final SsoConfigurationRepository ssoConfigurationRepository;

    public SsoConfigurationLoader(SsoConfigurationRepository ssoConfigurationRepository) {
        this.ssoConfigurationRepository = ssoConfigurationRepository;
    }

    public SsoConfiguration load(Long id) {
        return ssoConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SSO configuration not found: " + id));
    }
}
