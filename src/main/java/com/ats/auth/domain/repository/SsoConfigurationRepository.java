package com.ats.auth.domain.repository;

import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.enums.SsoProviderType;

import java.util.Optional;

/** Finders ignore soft-deleted configurations. */
public interface SsoConfigurationRepository {

    SsoConfiguration save(SsoConfiguration configuration);

    Optional<SsoConfiguration> findById(Long id);

    Optional<SsoConfiguration> findActiveByProviderType(SsoProviderType providerType);
}
