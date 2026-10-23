package com.ats.auth.application.usecase;

import com.ats.auth.application.response.AuthTokenResponse;
import com.ats.auth.application.service.AuthSessionService;
import com.ats.auth.application.service.AuthSessionService.IssuedSession;
import com.ats.auth.domain.aggregate.RefreshToken;
import com.ats.auth.domain.repository.RefreshTokenRepository;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.security.SecureTokens;
import com.ats.user.application.contract.UserAccountContract;
import com.ats.user.application.response.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Rotates the refresh token: the old one stops working and the response carries a new one.
 * Reusing an already rotated token means it has leaked, so every active session of that user is revoked.
 */
@Service
public class RefreshSessionUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshSessionUseCase.class);
    private static final String INVALID_REFRESH_TOKEN = "Refresh token is invalid or expired";

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountContract userAccounts;
    private final AuthSessionService authSessionService;
    private final Clock clock;

    public RefreshSessionUseCase(RefreshTokenRepository refreshTokenRepository,
                                 UserAccountContract userAccounts,
                                 AuthSessionService authSessionService,
                                 Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccounts = userAccounts;
        this.authSessionService = authSessionService;
        this.clock = clock;
    }

    /** The revocations must survive the exception thrown afterwards, hence {@code noRollbackFor}. */
    @Transactional(noRollbackFor = AuthenticationFailedException.class)
    public AuthTokenResponse execute(String rawRefreshToken) {
        RefreshToken current = findLocked(rawRefreshToken);
        Instant now = clock.instant();

        if (current.isRevoked()) {
            if (RefreshToken.REASON_ROTATED.equals(current.getRevokeReason())) {
                int revoked = refreshTokenRepository.revokeAllActiveByUserId(current.getUserId(),
                        RefreshToken.REASON_REUSE_DETECTED, now);
                log.warn("Refresh token reuse detected for user {}; revoked {} active session(s)",
                        current.getUserId(), revoked);
            }
            throw new AuthenticationFailedException(INVALID_REFRESH_TOKEN);
        }
        if (current.isExpired(now)) {
            throw new AuthenticationFailedException(INVALID_REFRESH_TOKEN);
        }

        UserResponse user = userAccounts.findById(current.getUserId())
                .filter(AuthSessionService::isActive)
                .orElse(null);
        if (user == null) {
            current.revoke(RefreshToken.REASON_LOGOUT, now);
            refreshTokenRepository.save(current);
            throw new AuthenticationFailedException("User is not active");
        }

        IssuedSession replacement = authSessionService.issue(user, now);
        current.rotateTo(replacement.refreshToken().getId(), now);
        refreshTokenRepository.save(current);
        return replacement.tokens();
    }

    private RefreshToken findLocked(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AuthenticationFailedException(INVALID_REFRESH_TOKEN);
        }
        return refreshTokenRepository.findByTokenHashForUpdate(SecureTokens.sha256Hex(rawRefreshToken.trim()))
                .orElseThrow(() -> new AuthenticationFailedException(INVALID_REFRESH_TOKEN));
    }
}
