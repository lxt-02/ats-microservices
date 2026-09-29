package com.ats.userservice.infrastructure.adapter;

import com.ats.userservice.domain.model.sso.aggregate.SsoConfiguration;
import com.ats.userservice.domain.model.sso.enums.SsoProviderType;
import com.ats.userservice.domain.repository.SsoConfigurationRepository;
import com.ats.userservice.infrastructure.mapper.SsoConfigurationPersistenceMapper;
import com.ats.userservice.infrastructure.persistence.repository.SpringDataSsoConfigurationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SsoConfigurationRepositoryAdapter implements SsoConfigurationRepository {

    private final SpringDataSsoConfigurationRepository springDataSsoConfigurationRepository;
    private final SsoConfigurationPersistenceMapper mapper;

    public SsoConfigurationRepositoryAdapter(
            SpringDataSsoConfigurationRepository springDataSsoConfigurationRepository,
            SsoConfigurationPersistenceMapper mapper
    ) {
        this.springDataSsoConfigurationRepository = springDataSsoConfigurationRepository;
        this.mapper = mapper;
    }

    @Override
    public SsoConfiguration save(SsoConfiguration configuration) {
        return mapper.toDomain(springDataSsoConfigurationRepository.save(mapper.toEntity(configuration)));
    }

    @Override
    public Optional<SsoConfiguration> findById(Long id) {
        return springDataSsoConfigurationRepository.findByIdAndDeletedFalse(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<SsoConfiguration> findActiveByProviderType(SsoProviderType providerType) {
        return springDataSsoConfigurationRepository.findFirstByProviderTypeAndActiveTrueAndDeletedFalse(providerType)
                .map(mapper::toDomain);
    }
}
