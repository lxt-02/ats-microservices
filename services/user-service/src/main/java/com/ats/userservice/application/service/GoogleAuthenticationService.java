package com.ats.userservice.application.service;

import com.ats.userservice.application.exception.AuthenticationFailedException;
import com.ats.userservice.application.exception.DuplicateResourceException;
import com.ats.userservice.domain.model.user.aggregate.User;
import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.enums.UserRole;
import com.ats.userservice.domain.model.user.valueobject.Email;
import com.ats.userservice.domain.model.user.valueobject.PhoneNumber;
import com.ats.userservice.domain.repository.UserRepository;
import com.ats.userservice.infrastructure.config.GoogleOAuthProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GoogleAuthenticationService {

    private static final String GOOGLE_ISSUER = "accounts.google.com";
    private static final String GOOGLE_ISSUER_URI = "https://accounts.google.com";
    private static final String SYSTEM_ACTOR = "google-oauth";

    private final GoogleOAuthProperties properties;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GoogleAuthenticationService(GoogleOAuthProperties properties, UserRepository userRepository,
                                       ObjectMapper objectMapper) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public URI buildAuthorizationUri(String state, String redirectUri) {
        requireGoogleClientId();
        String resolvedRedirectUri = resolveRedirectUri(redirectUri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getAuthorizationUri())
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", resolvedRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", properties.scopeValue())
                .queryParam("access_type", "offline")
                .queryParam("prompt", "select_account");

        if (state != null && !state.isBlank()) {
            builder.queryParam("state", state.trim());
        }

        return builder.build().encode().toUri();
    }

    public GoogleAuthenticationResult loginWithAuthorizationCode(String code, String redirectUri) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Authorization code is required");
        }
        requireGoogleClientId();
        requireGoogleClientSecret();

        JsonNode tokenResponse = exchangeAuthorizationCode(code.trim(), resolveRedirectUri(redirectUri));
        String idToken = requiredText(tokenResponse, "id_token", "Google token response does not include id_token");
        GoogleProfile profile = fetchAndValidateProfile(idToken);
        User user = findOrCreateGoogleUser(profile);
        return new GoogleAuthenticationResult(user, profile);
    }

    public GoogleAuthenticationResult loginWithIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Google ID token is required");
        }
        requireGoogleClientId();

        GoogleProfile profile = fetchAndValidateProfile(idToken.trim());
        User user = findOrCreateGoogleUser(profile);
        return new GoogleAuthenticationResult(user, profile);
    }

    private JsonNode exchangeAuthorizationCode(String code, String redirectUri) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("client_id", properties.getClientId());
        form.put("client_secret", properties.getClientSecret());
        form.put("code", code);
        form.put("grant_type", "authorization_code");
        form.put("redirect_uri", redirectUri);

        HttpRequest request = HttpRequest.newBuilder(URI.create(properties.getTokenUri()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(formEncode(form)))
                .build();

        return sendJson(request, "Google authorization code exchange failed");
    }

    private GoogleProfile fetchAndValidateProfile(String idToken) {
        URI tokenInfoUri = UriComponentsBuilder.fromUriString(properties.getTokenInfoUri())
                .queryParam("id_token", idToken)
                .build()
                .encode()
                .toUri();

        JsonNode tokenInfo = sendJson(HttpRequest.newBuilder(tokenInfoUri).GET().build(),
                "Google ID token verification failed");
        GoogleProfile profile = GoogleProfile.from(tokenInfo);
        validateProfile(profile);
        return profile;
    }

    private JsonNode sendJson(HttpRequest request, String failureMessage) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = response.body() == null || response.body().isBlank()
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String error = body.path("error_description").asText(body.path("error").asText(failureMessage));
                throw new AuthenticationFailedException(error);
            }

            return body;
        } catch (IOException ex) {
            throw new AuthenticationFailedException(failureMessage, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AuthenticationFailedException(failureMessage, ex);
        }
    }

    private void validateProfile(GoogleProfile profile) {
        if (!properties.getClientId().equals(profile.audience())) {
            throw new AuthenticationFailedException("Google token audience does not match configured client id");
        }
        if (!GOOGLE_ISSUER.equals(profile.issuer()) && !GOOGLE_ISSUER_URI.equals(profile.issuer())) {
            throw new AuthenticationFailedException("Google token issuer is invalid");
        }
        if (profile.expiresAtEpochSeconds() != null
                && Instant.ofEpochSecond(profile.expiresAtEpochSeconds()).isBefore(Instant.now())) {
            throw new AuthenticationFailedException("Google token is expired");
        }
        if (!profile.emailVerified()) {
            throw new AuthenticationFailedException("Google email is not verified");
        }
    }

    private User findOrCreateGoogleUser(GoogleProfile profile) {
        Optional<User> existingExternalUser = userRepository
                .findByAuthProviderAndExternalSubjectId(AuthProvider.GOOGLE, profile.subject());
        if (existingExternalUser.isPresent()) {
            return existingExternalUser.get();
        }

        Email email = Email.of(profile.email());
        Optional<User> existingEmailUser = userRepository.findByEmail(email);
        if (existingEmailUser.isPresent()) {
            throw new DuplicateResourceException("Email already exists with another auth provider: " + email.value());
        }

        User user = User.createExternal(profile.displayName(), email, PhoneNumber.of(null), UserRole.CANDIDATE,
                null, AuthProvider.GOOGLE, profile.subject(), SYSTEM_ACTOR);
        return userRepository.save(user);
    }

    private String resolveRedirectUri(String requestRedirectUri) {
        if (requestRedirectUri != null && !requestRedirectUri.isBlank()) {
            return requestRedirectUri.trim();
        }
        if (properties.getRedirectUri() == null) {
            throw new IllegalStateException("Google redirect URI is not configured");
        }
        return properties.getRedirectUri();
    }

    private void requireGoogleClientId() {
        if (properties.getClientId() == null) {
            throw new IllegalStateException("Google client id is not configured");
        }
    }

    private void requireGoogleClientSecret() {
        if (properties.getClientSecret() == null) {
            throw new IllegalStateException("Google client secret is not configured");
        }
    }

    private static String requiredText(JsonNode node, String field, String message) {
        String value = node.path(field).asText(null);
        if (value == null || value.isBlank()) {
            throw new AuthenticationFailedException(message);
        }
        return value;
    }

    private static String formEncode(Map<String, String> values) {
        return values.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record GoogleAuthenticationResult(User user, GoogleProfile profile) {
    }

    public record GoogleProfile(
            String subject,
            String audience,
            String issuer,
            String email,
            boolean emailVerified,
            String name,
            String pictureUrl,
            Long expiresAtEpochSeconds
    ) {

        private static GoogleProfile from(JsonNode tokenInfo) {
            String subject = requiredText(tokenInfo, "sub", "Google token does not include subject");
            String email = requiredText(tokenInfo, "email", "Google token does not include email");
            String audience = requiredText(tokenInfo, "aud", "Google token does not include audience");
            String issuer = requiredText(tokenInfo, "iss", "Google token does not include issuer");
            String emailVerifiedValue = tokenInfo.path("email_verified").asText("false");
            Long expiresAt = tokenInfo.hasNonNull("exp") ? tokenInfo.path("exp").asLong() : null;

            return new GoogleProfile(
                    subject,
                    audience,
                    issuer,
                    email,
                    Boolean.parseBoolean(emailVerifiedValue),
                    tokenInfo.path("name").asText(null),
                    tokenInfo.path("picture").asText(null),
                    expiresAt
            );
        }

        public String displayName() {
            if (name != null && !name.isBlank()) {
                return name.trim();
            }
            return email;
        }
    }
}
