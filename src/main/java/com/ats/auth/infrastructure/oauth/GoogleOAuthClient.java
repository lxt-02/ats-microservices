package com.ats.auth.infrastructure.oauth;

import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.exception.ExternalServiceException;
import com.ats.auth.application.port.out.GoogleIdentityProvider;
import com.ats.auth.infrastructure.configuration.GoogleOAuthProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Google adapter: builds the consent URL, exchanges authorization codes and verifies ID tokens locally
 * against Google's JWKS (no round-trip to the tokeninfo endpoint per login).
 */
@Component
public class GoogleOAuthClient implements GoogleIdentityProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthClient.class);
    private static final Set<String> GOOGLE_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");
    private static final String INVALID_TOKEN = "invalid_token";

    private final GoogleOAuthProperties properties;
    private final RestClient restClient;
    private final JwtDecoder idTokenDecoder;

    public GoogleOAuthClient(GoogleOAuthProperties properties, ObjectProvider<RestClient.Builder> restClientBuilder) {
        this.properties = properties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        this.restClient = restClientBuilder.getIfAvailable(RestClient::builder).requestFactory(requestFactory).build();

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri())
                .restOperations(new RestTemplate(requestFactory))
                .build();
        OAuth2TokenValidator<Jwt> googleClaimsValidator = this::validateGoogleClaims;
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                List.of(new JwtTimestampValidator(), googleClaimsValidator)));
        this.idTokenDecoder = decoder;
    }

    @Override
    public String resolveRedirectUri(String requestedRedirectUri) {
        if (requestedRedirectUri == null || requestedRedirectUri.isBlank()) {
            if (properties.getRedirectUri() == null) {
                throw new IllegalStateException("Google redirect URI is not configured");
            }
            return properties.getRedirectUri();
        }
        String redirectUri = requestedRedirectUri.trim();
        if (!properties.effectiveAllowedRedirectUris().contains(redirectUri)) {
            throw new IllegalArgumentException("Redirect URI is not allowed");
        }
        return redirectUri;
    }

    @Override
    public URI buildAuthorizationUri(String state, String codeChallenge, String redirectUri) {
        return UriComponentsBuilder.fromUriString(properties.getAuthorizationUri())
                .queryParam("client_id", requireClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", properties.scopeValue())
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("prompt", "select_account")
                .build()
                .encode()
                .toUri();
    }

    @Override
    public GoogleIdentity exchangeAuthorizationCode(String code, String codeVerifier, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", requireClientId());
        form.add("client_secret", requireClientSecret());
        form.add("code", code);
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", redirectUri);
        if (codeVerifier != null) {
            form.add("code_verifier", codeVerifier);
        }

        GoogleTokenResponse response;
        try {
            response = restClient.post()
                    .uri(properties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
        } catch (HttpClientErrorException ex) {
            log.info("Google rejected authorization code: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AuthenticationFailedException("Google rejected the authorization code");
        } catch (RestClientException ex) {
            log.warn("Google token endpoint call failed", ex);
            throw new ExternalServiceException("Google sign-in is temporarily unavailable", ex);
        }

        if (response == null || response.idToken() == null || response.idToken().isBlank()) {
            throw new AuthenticationFailedException("Google token response does not include id_token");
        }
        return verifyIdToken(response.idToken());
    }

    @Override
    public GoogleIdentity verifyIdToken(String idToken) {
        requireClientId();
        Jwt jwt;
        try {
            jwt = idTokenDecoder.decode(idToken);
        } catch (BadJwtException ex) {
            log.info("Rejected Google ID token: {}", ex.getMessage());
            throw new AuthenticationFailedException("Google ID token is invalid");
        } catch (JwtException ex) {
            // Remote JWKS could not be fetched or parsed
            log.warn("Could not verify Google ID token", ex);
            throw new ExternalServiceException("Google sign-in is temporarily unavailable", ex);
        }

        return new GoogleIdentity(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("picture")
        );
    }

    private OAuth2TokenValidatorResult validateGoogleClaims(Jwt jwt) {
        if (!GOOGLE_ISSUERS.contains(jwt.getClaimAsString("iss"))) {
            return failure("Token issuer is not Google");
        }
        List<String> audience = jwt.getAudience();
        if (audience == null || !audience.contains(properties.getClientId())) {
            return failure("Token audience does not match the configured client id");
        }
        if (isBlank(jwt.getSubject()) || isBlank(jwt.getClaimAsString("email"))) {
            return failure("Token does not include subject or email");
        }
        Object emailVerified = jwt.getClaims().get("email_verified");
        if (!Boolean.TRUE.equals(emailVerified) && !"true".equalsIgnoreCase(String.valueOf(emailVerified))) {
            return failure("Google email is not verified");
        }
        return OAuth2TokenValidatorResult.success();
    }

    private String requireClientId() {
        if (properties.getClientId() == null) {
            throw new IllegalStateException("Google client id is not configured");
        }
        return properties.getClientId();
    }

    private String requireClientSecret() {
        if (properties.getClientSecret() == null) {
            throw new IllegalStateException("Google client secret is not configured");
        }
        return properties.getClientSecret();
    }

    private static OAuth2TokenValidatorResult failure(String description) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(INVALID_TOKEN, description, null));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleTokenResponse(@JsonProperty("id_token") String idToken) {
    }
}
