package com.ats.userservice.domain.model.sso.aggregate;

import com.ats.userservice.domain.model.sso.enums.SsoProviderType;
import com.ats.userservice.domain.model.sso.exception.SsoConfigurationDomainException;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class SsoConfiguration {

    private static final Set<String> DEFAULT_SCOPES = Set.of("openid", "profile", "email");

    private final Long id;
    private final SsoProviderType providerType;
    private String clientId;
    private String clientSecretRef;
    private String issuerUri;
    private String redirectUri;
    private Set<String> scopes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;

    private SsoConfiguration(Long id, SsoProviderType providerType, String clientId, String clientSecretRef,
                             String issuerUri, String redirectUri, Set<String> scopes, boolean active,
                             Instant createdAt, Instant updatedAt, String createdBy, String updatedBy,
                             boolean deleted, Instant deletedAt) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("SSO configuration id must be positive");
        }
        this.id = id;
        this.providerType = Objects.requireNonNull(providerType, "SSO provider type is required");
        this.clientId = requireText(clientId, "Client id is required");
        this.clientSecretRef = normalizeNullable(clientSecretRef);
        this.issuerUri = requireText(issuerUri, "Issuer URI is required");
        this.redirectUri = normalizeNullable(redirectUri);
        this.scopes = normalizeScopes(scopes);
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = normalizeNullable(createdBy);
        this.updatedBy = normalizeNullable(updatedBy);
        this.deleted = deleted;
        this.deletedAt = deletedAt;
        validateProviderConfiguration();
    }

    public static SsoConfiguration createKeycloak(String clientId, String clientSecretRef, String issuerUri,
                                                  String redirectUri, Set<String> scopes, String createdBy) {
        Instant now = Instant.now();
        return new SsoConfiguration(null, SsoProviderType.KEYCLOAK, clientId, clientSecretRef, issuerUri,
                redirectUri, scopes, true, now, now, createdBy, createdBy, false, null);
    }

    public static SsoConfiguration createGoogle(String clientId, String clientSecretRef, String issuerUri,
                                                String redirectUri, Set<String> scopes, String createdBy) {
        Instant now = Instant.now();
        return new SsoConfiguration(null, SsoProviderType.GOOGLE, clientId, clientSecretRef, issuerUri,
                redirectUri, scopes, true, now, now, createdBy, createdBy, false, null);
    }

    public static SsoConfiguration restore(Long id, SsoProviderType providerType, String clientId,
                                           String clientSecretRef, String issuerUri, String redirectUri,
                                           Set<String> scopes, boolean active, Instant createdAt,
                                           Instant updatedAt, String createdBy, String updatedBy,
                                           boolean deleted, Instant deletedAt) {
        return new SsoConfiguration(id, providerType, clientId, clientSecretRef, issuerUri, redirectUri,
                scopes, active, createdAt, updatedAt, createdBy, updatedBy, deleted, deletedAt);
    }

    public void updateClient(String clientId, String clientSecretRef, String updatedBy) {
        ensureNotDeleted();
        this.clientId = requireText(clientId, "Client id is required");
        this.clientSecretRef = normalizeNullable(clientSecretRef);
        markUpdated(updatedBy);
    }

    public void updateIssuer(String issuerUri, String updatedBy) {
        ensureNotDeleted();
        this.issuerUri = requireText(issuerUri, "Issuer URI is required");
        validateProviderConfiguration();
        markUpdated(updatedBy);
    }

    public void updateRedirectAndScopes(String redirectUri, Set<String> scopes, String updatedBy) {
        ensureNotDeleted();
        this.redirectUri = normalizeNullable(redirectUri);
        this.scopes = normalizeScopes(scopes);
        markUpdated(updatedBy);
    }

    public void activate(String updatedBy) {
        ensureNotDeleted();
        this.active = true;
        markUpdated(updatedBy);
    }

    public void deactivate(String updatedBy) {
        ensureNotDeleted();
        this.active = false;
        markUpdated(updatedBy);
    }

    public void markDeleted(String updatedBy) {
        if (deleted) {
            return;
        }
        this.deleted = true;
        this.deletedAt = Instant.now();
        markUpdated(updatedBy);
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new SsoConfigurationDomainException("Deleted SSO configuration cannot be changed");
        }
    }

    private void validateProviderConfiguration() {
        if (providerType != SsoProviderType.KEYCLOAK && providerType != SsoProviderType.GOOGLE) {
            throw new SsoConfigurationDomainException("Unsupported SSO provider type");
        }
    }

    private void markUpdated(String updatedBy) {
        this.updatedAt = Instant.now();
        this.updatedBy = normalizeNullable(updatedBy);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static Set<String> normalizeScopes(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return DEFAULT_SCOPES;
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                normalized.add(value.trim());
            }
        }

        return normalized.isEmpty() ? DEFAULT_SCOPES : Collections.unmodifiableSet(normalized);
    }

    public Long getId() {
        return id;
    }

    public SsoProviderType getProviderType() {
        return providerType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecretRef() {
        return clientSecretRef;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
