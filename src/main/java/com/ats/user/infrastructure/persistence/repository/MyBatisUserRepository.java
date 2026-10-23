package com.ats.user.infrastructure.persistence.repository;

import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.AuthProvider;
import com.ats.user.domain.repository.UserRepository;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.UserId;
import com.ats.user.infrastructure.persistence.converter.UserPersistenceConverter;
import com.ats.user.infrastructure.persistence.mapper.UserMyBatisMapper;
import com.ats.user.infrastructure.persistence.record.UserRecord;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisUserRepository implements UserRepository {

    private final UserMyBatisMapper mapper;

    public MyBatisUserRepository(UserMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserRecord record = UserPersistenceConverter.toRecord(user);
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.update(record);
        }
        return UserPersistenceConverter.toAggregate(record);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return mapper.findActiveById(id.value()).map(UserPersistenceConverter::toAggregate);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return mapper.findActiveByEmail(email.value()).map(UserPersistenceConverter::toAggregate);
    }

    @Override
    public Optional<User> findByAuthProviderAndExternalSubjectId(AuthProvider authProvider, String externalSubjectId) {
        return mapper.findActiveByExternalIdentity(authProvider.name(), externalSubjectId)
                .map(UserPersistenceConverter::toAggregate);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return mapper.existsActiveByEmail(email.value());
    }
}
