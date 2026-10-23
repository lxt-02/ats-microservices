package com.ats.user.domain.repository;

import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.AuthProvider;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.UserId;

import java.util.Optional;

/** All finders ignore soft-deleted users. */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    Optional<User> findByAuthProviderAndExternalSubjectId(AuthProvider authProvider, String externalSubjectId);

    boolean existsByEmail(Email email);
}
