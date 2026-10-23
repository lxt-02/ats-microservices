package com.ats.auth.application.usecase;

import com.ats.auth.domain.aggregate.RefreshToken;
import com.ats.auth.domain.repository.RefreshTokenRepository;
import com.ats.shared.security.SecureTokens;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Revokes a refresh token. Idempotent: an unknown or already revoked token is silently ignored. */
@Service
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Transactional
    public void execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHashForUpdate(SecureTokens.sha256Hex(rawRefreshToken.trim()))
                .ifPresent(token -> {
                    token.revoke(RefreshToken.REASON_LOGOUT, clock.instant());
                    refreshTokenRepository.save(token);
                });
    }
}
