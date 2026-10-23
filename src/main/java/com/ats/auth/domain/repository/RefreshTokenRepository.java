package com.ats.auth.domain.repository;

import com.ats.auth.domain.aggregate.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    /**
     * Finds a token by hash and locks the row for the current transaction,
     * so two concurrent refreshes with the same token cannot both succeed.
     */
    Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);

    int revokeAllActiveByUserId(Long userId, String reason, Instant now);
}
