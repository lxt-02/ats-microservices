package com.ats.userservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private Set<String> scopes = new LinkedHashSet<>(Set.of("openid", "profile", "email"));
    private String authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth";
    private String tokenUri = "https://oauth2.googleapis.com/token";
    private String tokenInfoUri = "https://oauth2.googleapis.com/tokeninfo";

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

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String scope : scopes) {
            if (scope != null && !scope.isBlank()) {
                normalized.add(scope.trim());
            }
        }
        if (!normalized.isEmpty()) {
            this.scopes = normalized;
        }
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

    public String getTokenInfoUri() {
        return tokenInfoUri;
    }

    public void setTokenInfoUri(String tokenInfoUri) {
        this.tokenInfoUri = normalizeRequired(tokenInfoUri, "Google token info URI is required");
    }

    public String scopeValue() {
        return String.join(" ", scopes);
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
