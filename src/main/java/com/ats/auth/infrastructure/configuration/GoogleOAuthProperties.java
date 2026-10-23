package com.ats.auth.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthProperties {

    private String clientId;
    private String clientSecret;
    /** Default redirect URI, always allowed. */
    private String redirectUri;
    /** Extra redirect URIs a client may request (e.g. frontend callback pages). Must also be registered in Google Console. */
    private Set<String> allowedRedirectUris = new LinkedHashSet<>();
    private Set<String> scopes = new LinkedHashSet<>(List.of("openid", "profile", "email"));
    private String authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth";
    private String tokenUri = "https://oauth2.googleapis.com/token";
    private String jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = normalizeNullable(clientId);
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = normalizeNullable(clientSecret);
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = normalizeNullable(redirectUri);
    }

    public Set<String> getAllowedRedirectUris() {
        return allowedRedirectUris;
    }

    public void setAllowedRedirectUris(Set<String> allowedRedirectUris) {
        this.allowedRedirectUris = normalizeSet(allowedRedirectUris);
    }

    /** Whitelist actually enforced: the default redirect URI plus the configured extra ones. */
    public Set<String> effectiveAllowedRedirectUris() {
        Set<String> allowed = new LinkedHashSet<>();
        if (redirectUri != null) {
            allowed.add(redirectUri);
        }
        allowed.addAll(allowedRedirectUris);
        return allowed;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        Set<String> normalized = normalizeSet(scopes);
        if (!normalized.isEmpty()) {
            this.scopes = normalized;
        }
    }

    public String scopeValue() {
        return String.join(" ", scopes);
    }

    public String getAuthorizationUri() {
        return authorizationUri;
    }

    public void setAuthorizationUri(String authorizationUri) {
        this.authorizationUri = normalizeRequired(authorizationUri, "Google authorization URI is required");
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = normalizeRequired(tokenUri, "Google token URI is required");
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = normalizeRequired(jwkSetUri, "Google JWK set URI is required");
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        if (connectTimeout != null) {
            this.connectTimeout = connectTimeout;
        }
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        if (readTimeout != null) {
            this.readTimeout = readTimeout;
        }
    }

    private static Set<String> normalizeSet(Set<String> values) {
        Set<String> normalized = new LinkedHashSet<>();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    normalized.add(value.trim());
                }
            }
        }
        return normalized;
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
