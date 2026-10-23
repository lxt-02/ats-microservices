-- Opaque refresh tokens (only the SHA-256 hash is stored), rotated on every refresh.
CREATE TABLE refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revoke_reason VARCHAR(50),
    replaced_by_id BIGINT,

    CONSTRAINT uq_refresh_tokens_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_id)
            REFERENCES refresh_tokens(id)
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);
