package com.ats.userservice.infrastructure.persistence.repository;

import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByIdAndDeletedFalse(Long id);

    Optional<UserJpaEntity> findByEmailAndDeletedFalse(String email);

    Optional<UserJpaEntity> findByAuthProviderAndExternalSubjectIdAndDeletedFalse(
            AuthProvider authProvider,
            String externalSubjectId
    );

    boolean existsByEmailAndDeletedFalse(String email);
}
