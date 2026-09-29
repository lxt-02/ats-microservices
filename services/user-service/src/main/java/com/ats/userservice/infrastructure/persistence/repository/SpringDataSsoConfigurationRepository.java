package com.ats.userservice.infrastructure.persistence.repository;

import com.ats.userservice.domain.model.sso.enums.SsoProviderType;
import com.ats.userservice.infrastructure.persistence.entity.SsoConfigurationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataSsoConfigurationRepository extends JpaRepository<SsoConfigurationJpaEntity, Long> {

    Optional<SsoConfigurationJpaEntity> findByIdAndDeletedFalse(Long id);

    Optional<SsoConfigurationJpaEntity> findFirstByProviderTypeAndActiveTrueAndDeletedFalse(SsoProviderType providerType);
}
