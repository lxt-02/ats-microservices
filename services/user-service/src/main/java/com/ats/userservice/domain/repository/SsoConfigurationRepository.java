package com.ats.userservice.domain.repository;

import com.ats.userservice.domain.model.sso.aggregate.SsoConfiguration;
import com.ats.userservice.domain.model.sso.enums.SsoProviderType;

import java.util.Optional;

public interface SsoConfigurationRepository {

    SsoConfiguration save(SsoConfiguration configuration);

    Optional<SsoConfiguration> findById(Long id);

    Optional<SsoConfiguration> findActiveByProviderType(SsoProviderType providerType);
}
