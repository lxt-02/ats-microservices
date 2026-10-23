package com.ats.auth.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code refresh_tokens}. */
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRecord {

    private Long id;
    private Long userId;
    private String tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokeReason;
    private Long replacedById;
}
