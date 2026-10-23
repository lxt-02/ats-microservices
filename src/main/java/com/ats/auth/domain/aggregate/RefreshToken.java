package com.ats.auth.domain.aggregate;

import java.time.Instant;
import java.util.Objects;

/**
 * Opaque refresh token issued by ATS. Only the SHA-256 hash of the raw token is persisted.
 * The owner is referenced by user id only (User module).
 * A token is single-use: refreshing revokes it and links it to its replacement (rotation).
 */
public class RefreshToken {

    public static final String REASON_ROTATED = "ROTATED";
    public static final String REASON_LOGOUT = "LOGOUT";
    public static final String REASON_REUSE_DETECTED = "REUSE_DETECTED";

    private final Long id;
    private final Long userId;
    private final String tokenHash;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant revokedAt;
    private String revokeReason;
    private Long replacedById;

    private RefreshToken(Long id, Long userId, String tokenHash, Instant createdAt, Instant expiresAt,
                         Instant revokedAt, String revokeReason, Long replacedById) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "User id is required");
        this.tokenHash = Objects.requireNonNull(tokenHash, "Token hash is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expires at is required");
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
        this.replacedById = replacedById;
    }

    public static RefreshToken issue(Long userId, String tokenHash, Instant now, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("Refresh token TTL must be positive");
        }
        return new RefreshToken(null, userId, tokenHash, now, now.plusSeconds(ttlSeconds), null, null, null);
    }

    public static RefreshToken restore(Long id, Long userId, String tokenHash, Instant createdAt, Instant expiresAt,
                                       Instant revokedAt, String revokeReason, Long replacedById) {
        return new RefreshToken(id, userId, tokenHash, createdAt, expiresAt, revokedAt, revokeReason, replacedById);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public void rotateTo(Long replacementId, Instant now) {
        revoke(REASON_ROTATED, now);
        this.replacedById = replacementId;
    }

    public void revoke(String reason, Instant now) {
        if (isRevoked()) {
            return;
        }
        this.revokedAt = now;
        this.revokeReason = reason;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public Long getReplacedById() {
        return replacedById;
    }
}
