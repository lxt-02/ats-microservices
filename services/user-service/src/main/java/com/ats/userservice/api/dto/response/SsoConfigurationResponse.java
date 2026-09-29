package com.ats.userservice.api.dto.response;

import com.ats.userservice.domain.model.sso.enums.SsoProviderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SsoConfigurationResponse {

    private Long id;
    private SsoProviderType providerType;
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
}
