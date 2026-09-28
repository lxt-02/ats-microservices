package com.ats.userservice.domain.repository;

import com.ats.userservice.domain.model.user.aggregate.User;
import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.valueobject.Email;
import com.ats.userservice.domain.model.user.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    Optional<User> findByAuthProviderAndExternalSubjectId(AuthProvider authProvider, String externalSubjectId);

    boolean existsByEmail(Email email);
}
