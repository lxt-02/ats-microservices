package com.ats.auth.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code sso_configurations}. {@code scopes} is a comma-separated list. */
@Getter
@Setter
@NoArgsConstructor
public class SsoConfigurationRecord {

    private Long id;
    private String providerType;
    private String clientId;
    private String clientSecretRef;
    private String issuerUri;
    private String redirectUri;
    private String scopes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;
}
