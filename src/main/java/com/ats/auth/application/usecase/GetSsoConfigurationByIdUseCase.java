package com.ats.auth.application.usecase;

import com.ats.auth.application.response.SsoConfigurationResponse;
import com.ats.auth.application.service.SsoConfigurationLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSsoConfigurationByIdUseCase {

    private final SsoConfigurationLoader ssoConfigurationLoader;

    public GetSsoConfigurationByIdUseCase(SsoConfigurationLoader ssoConfigurationLoader) {
        this.ssoConfigurationLoader = ssoConfigurationLoader;
    }

    @Transactional(readOnly = true)
    public SsoConfigurationResponse execute(Long id) {
        return SsoConfigurationResponse.from(ssoConfigurationLoader.load(id));
    }
}
