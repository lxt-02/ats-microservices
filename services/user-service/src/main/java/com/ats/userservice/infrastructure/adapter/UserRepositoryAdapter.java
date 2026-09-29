package com.ats.userservice.infrastructure.adapter;

import com.ats.userservice.domain.model.user.aggregate.User;
import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.valueobject.Email;
import com.ats.userservice.domain.model.user.valueobject.UserId;
import com.ats.userservice.domain.repository.UserRepository;
import com.ats.userservice.infrastructure.mapper.UserPersistenceMapper;
import com.ats.userservice.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserPersistenceMapper mapper;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository, UserPersistenceMapper mapper) {
        this.springDataUserRepository = springDataUserRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(springDataUserRepository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springDataUserRepository.findByIdAndDeletedFalse(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataUserRepository.findByEmailAndDeletedFalse(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByAuthProviderAndExternalSubjectId(AuthProvider authProvider, String externalSubjectId) {
        return springDataUserRepository
                .findByAuthProviderAndExternalSubjectIdAndDeletedFalse(authProvider, externalSubjectId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataUserRepository.existsByEmailAndDeletedFalse(email.value());
    }
}
