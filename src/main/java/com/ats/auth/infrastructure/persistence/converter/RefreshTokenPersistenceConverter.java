package com.ats.auth.infrastructure.persistence.converter;

import com.ats.auth.domain.aggregate.RefreshToken;
import com.ats.auth.infrastructure.persistence.record.RefreshTokenRecord;

public final class RefreshTokenPersistenceConverter {

    private RefreshTokenPersistenceConverter() {
    }

    public static RefreshTokenRecord toRecord(RefreshToken token) {
        RefreshTokenRecord record = new RefreshTokenRecord();
        record.setId(token.getId());
        record.setUserId(token.getUserId());
        record.setTokenHash(token.getTokenHash());
        record.setCreatedAt(token.getCreatedAt());
        record.setExpiresAt(token.getExpiresAt());
        record.setRevokedAt(token.getRevokedAt());
        record.setRevokeReason(token.getRevokeReason());
        record.setReplacedById(token.getReplacedById());
        return record;
    }

    public static RefreshToken toAggregate(RefreshTokenRecord record) {
        return RefreshToken.restore(
                record.getId(),
                record.getUserId(),
                record.getTokenHash(),
                record.getCreatedAt(),
                record.getExpiresAt(),
                record.getRevokedAt(),
                record.getRevokeReason(),
                record.getReplacedById()
        );
    }
}
