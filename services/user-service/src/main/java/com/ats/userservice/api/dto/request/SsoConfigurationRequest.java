package com.ats.userservice.api.dto.request;

import com.ats.userservice.domain.model.sso.enums.SsoProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SsoConfigurationRequest {

    private SsoProviderType providerType;

    @NotBlank(message = "Client id is required")
    @Size(max = 500, message = "Client id must not exceed 500 characters")
    private String clientId;

    @Size(max = 500, message = "Client secret reference must not exceed 500 characters")
    private String clientSecretRef;

    @NotBlank(message = "Issuer URI is required")
    @Size(max = 500, message = "Issuer URI must not exceed 500 characters")
    private String issuerUri;

    @Size(max = 500, message = "Redirect URI must not exceed 500 characters")
    private String redirectUri;

    private Set<String> scopes;

    @Size(max = 255, message = "Actor must not exceed 255 characters")
    private String actor;

}
