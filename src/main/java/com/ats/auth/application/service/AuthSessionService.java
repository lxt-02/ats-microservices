package com.ats.auth.application.service;

import com.ats.auth.application.port.out.AccessTokenIssuer;
import com.ats.auth.application.port.out.AccessTokenIssuer.IssuedAccessToken;
import com.ats.auth.application.response.AuthTokenResponse;
import com.ats.auth.domain.aggregate.RefreshToken;
import com.ats.auth.domain.repository.RefreshTokenRepository;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.security.SecureTokens;
import com.ats.user.application.contract.UserAccountContract;
import com.ats.user.application.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Issues ATS sessions: a JWT access token plus an opaque refresh token of which only the SHA-256 hash is stored.
 * Used by every login use case and by the refresh use case.
 */
@Service
public class AuthSessionService {

    private static final int REFRESH_TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenIssuer accessTokenIssuer;
    private final long refreshTokenTtlSeconds;
    private final Clock clock;

    public AuthSessionService(RefreshTokenRepository refreshTokenRepository,
                              AccessTokenIssuer accessTokenIssuer,
                              @Value("${ats.security.jwt.refresh-token-ttl-seconds:604800}") long refreshTokenTtlSeconds,
                              Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.clock = clock;
    }

    /** Opens a new session for a user who has just authenticated. */
    @Transactional
    public AuthTokenResponse open(UserResponse user) {
        ensureActive(user);
        return issue(user, clock.instant()).tokens();
    }

    /** Stores a new refresh token and signs an access token. The caller checks that the user is active. */
    public IssuedSession issue(UserResponse user, Instant now) {
        String rawRefreshToken = SecureTokens.randomUrlSafe(REFRESH_TOKEN_BYTES);
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.issue(user.id(),
                SecureTokens.sha256Hex(rawRefreshToken), now, refreshTokenTtlSeconds));
        IssuedAccessToken accessToken = accessTokenIssuer.issue(user.id(), List.of(user.role()));
        AuthTokenResponse tokens = new AuthTokenResponse(AuthTokenResponse.BEARER, accessToken.token(),
                accessToken.expiresInSeconds(), rawRefreshToken, refreshTokenTtlSeconds);
        return new IssuedSession(refreshToken, tokens);
    }

    public static boolean isActive(UserResponse user) {
        return user != null && UserAccountContract.ACTIVE_STATUS.equals(user.status());
    }

    public static void ensureActive(UserResponse user) {
        if (!isActive(user)) {
            throw new AuthenticationFailedException("User is not active");
        }
    }

    public record IssuedSession(RefreshToken refreshToken, AuthTokenResponse tokens) {
    }
}
