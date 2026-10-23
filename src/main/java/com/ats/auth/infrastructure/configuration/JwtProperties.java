package com.ats.auth.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ats.security.jwt")
public class JwtProperties {

    private String secret;
    private String issuer = "ats-auth-service";
    private long accessTokenTtlSeconds = 900;
    private long refreshTokenTtlSeconds = 604800;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret == null || secret.isBlank() ? null : secret.trim();
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        if (issuer != null && !issuer.isBlank()) {
            this.issuer = issuer.trim();
        }
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtlSeconds;
    }

    public void setRefreshTokenTtlSeconds(long refreshTokenTtlSeconds) {
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }
}
