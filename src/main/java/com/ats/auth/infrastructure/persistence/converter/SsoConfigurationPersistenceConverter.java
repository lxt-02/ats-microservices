package com.ats.auth.infrastructure.persistence.converter;

import com.ats.auth.domain.aggregate.SsoConfiguration;
import com.ats.auth.domain.enums.SsoProviderType;
import com.ats.auth.infrastructure.persistence.record.SsoConfigurationRecord;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class SsoConfigurationPersistenceConverter {

    private SsoConfigurationPersistenceConverter() {
    }

    public static SsoConfigurationRecord toRecord(SsoConfiguration configuration) {
        SsoConfigurationRecord record = new SsoConfigurationRecord();
        record.setId(configuration.getId());
        record.setProviderType(configuration.getProviderType().name());
        record.setClientId(configuration.getClientId());
        record.setClientSecretRef(configuration.getClientSecretRef());
        record.setIssuerUri(configuration.getIssuerUri());
        record.setRedirectUri(configuration.getRedirectUri());
        record.setScopes(toScopeString(configuration.getScopes()));
        record.setActive(configuration.isActive());
        record.setCreatedAt(configuration.getCreatedAt());
        record.setUpdatedAt(configuration.getUpdatedAt());
        record.setCreatedBy(configuration.getCreatedBy());
        record.setUpdatedBy(configuration.getUpdatedBy());
        record.setDeleted(configuration.isDeleted());
        record.setDeletedAt(configuration.getDeletedAt());
        return record;
    }

    public static SsoConfiguration toAggregate(SsoConfigurationRecord record) {
        return SsoConfiguration.restore(
                record.getId(),
                SsoProviderType.valueOf(record.getProviderType()),
                record.getClientId(),
                record.getClientSecretRef(),
                record.getIssuerUri(),
                record.getRedirectUri(),
                toScopeSet(record.getScopes()),
                record.isActive(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getCreatedBy(),
                record.getUpdatedBy(),
                record.isDeleted(),
                record.getDeletedAt()
        );
    }

    private static String toScopeString(Set<String> scopes) {
        return scopes == null || scopes.isEmpty() ? null : String.join(",", scopes);
    }

    private static Set<String> toScopeSet(String scopes) {
        if (scopes == null || scopes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(scopes.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
