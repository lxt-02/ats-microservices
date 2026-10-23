package com.ats.auth.infrastructure.persistence.repository;

import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.enums.SsoProviderType;
import com.ats.auth.domain.repository.SsoConfigurationRepository;
import com.ats.auth.infrastructure.persistence.converter.SsoConfigurationPersistenceConverter;
import com.ats.auth.infrastructure.persistence.mapper.SsoConfigurationMyBatisMapper;
import com.ats.auth.infrastructure.persistence.record.SsoConfigurationRecord;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisSsoConfigurationRepository implements SsoConfigurationRepository {

    private final SsoConfigurationMyBatisMapper mapper;

    public MyBatisSsoConfigurationRepository(SsoConfigurationMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SsoConfiguration save(SsoConfiguration configuration) {
        SsoConfigurationRecord record = SsoConfigurationPersistenceConverter.toRecord(configuration);
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.update(record);
        }
        return SsoConfigurationPersistenceConverter.toAggregate(record);
    }

    @Override
    public Optional<SsoConfiguration> findById(Long id) {
        return mapper.findActiveById(id).map(SsoConfigurationPersistenceConverter::toAggregate);
    }

    @Override
    public Optional<SsoConfiguration> findActiveByProviderType(SsoProviderType providerType) {
        return mapper.findEnabledByProviderType(providerType.name())
                .map(SsoConfigurationPersistenceConverter::toAggregate);
    }
}
