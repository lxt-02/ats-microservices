package com.ats.userservice.infrastructure.mapper;

import com.ats.userservice.domain.model.sso.aggregate.SsoConfiguration;
import com.ats.userservice.infrastructure.persistence.entity.SsoConfigurationJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SsoConfigurationPersistenceMapper {

    public SsoConfigurationJpaEntity toEntity(SsoConfiguration configuration) {
        SsoConfigurationJpaEntity entity = new SsoConfigurationJpaEntity();
        entity.setId(configuration.getId());
        entity.setProviderType(configuration.getProviderType());
        entity.setClientId(configuration.getClientId());
        entity.setClientSecretRef(configuration.getClientSecretRef());
        entity.setIssuerUri(configuration.getIssuerUri());
        entity.setRedirectUri(configuration.getRedirectUri());
        entity.setScopes(toScopeString(configuration.getScopes()));
        entity.setActive(configuration.isActive());
        entity.setCreatedAt(configuration.getCreatedAt());
        entity.setUpdatedAt(configuration.getUpdatedAt());
        entity.setCreatedBy(configuration.getCreatedBy());
        entity.setUpdatedBy(configuration.getUpdatedBy());
        entity.setDeleted(configuration.isDeleted());
        entity.setDeletedAt(configuration.getDeletedAt());
        return entity;
    }

    public SsoConfiguration toDomain(SsoConfigurationJpaEntity entity) {
        return SsoConfiguration.restore(
                entity.getId(),
                entity.getProviderType(),
                entity.getClientId(),
                entity.getClientSecretRef(),
                entity.getIssuerUri(),
                entity.getRedirectUri(),
                toScopeSet(entity.getScopes()),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.isDeleted(),
                entity.getDeletedAt()
        );
    }

    private String toScopeString(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return null;
        }
        return String.join(",", scopes);
    }

    private Set<String> toScopeSet(String scopes) {
        if (scopes == null || scopes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(scopes.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
