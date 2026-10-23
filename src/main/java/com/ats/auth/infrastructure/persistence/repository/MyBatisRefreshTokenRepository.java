package com.ats.auth.infrastructure.persistence.repository;

import com.ats.auth.domain.aggregate.RefreshToken;
import com.ats.auth.domain.repository.RefreshTokenRepository;
import com.ats.auth.infrastructure.persistence.converter.RefreshTokenPersistenceConverter;
import com.ats.auth.infrastructure.persistence.mapper.RefreshTokenMyBatisMapper;
import com.ats.auth.infrastructure.persistence.record.RefreshTokenRecord;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class MyBatisRefreshTokenRepository implements RefreshTokenRepository {

    private final RefreshTokenMyBatisMapper mapper;

    public MyBatisRefreshTokenRepository(RefreshTokenMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenRecord record = RefreshTokenPersistenceConverter.toRecord(refreshToken);
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.update(record);
        }
        return RefreshTokenPersistenceConverter.toAggregate(record);
    }

    @Override
    public Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash) {
        return mapper.findByTokenHashForUpdate(tokenHash).map(RefreshTokenPersistenceConverter::toAggregate);
    }

    @Override
    public int revokeAllActiveByUserId(Long userId, String reason, Instant now) {
        return mapper.revokeAllActiveByUserId(userId, reason, now);
    }
}
