package com.ats.auth.api.http;

import com.ats.auth.application.response.GoogleAuthorizationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * Binds a Google login to the browser that started it (login-CSRF protection).
 * Holds state, PKCE verifier and redirect URI in a short-lived HttpOnly cookie
 * scoped to the Google auth endpoints. Sent back by the browser on Google's
 * top-level redirect to the callback (SameSite=Lax).
 */
@Component
public class GoogleOAuthStateCookie {

    public static final String NAME = "ATS_GOOGLE_OAUTH";
    private static final String PATH = "/api/v1/auth/google";
    private static final Duration MAX_AGE = Duration.ofMinutes(5);
    private static final String SEPARATOR = ".";

    private final boolean secure;

    public GoogleOAuthStateCookie(@Value("${ats.security.oauth-state-cookie.secure:true}") boolean secure) {
        this.secure = secure;
    }

    public ResponseCookie create(GoogleAuthorizationResponse request) {
        String value = request.state() + SEPARATOR + request.codeVerifier() + SEPARATOR
                + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(request.redirectUri().getBytes(StandardCharsets.UTF_8));
        return base(value).maxAge(MAX_AGE).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(Duration.ZERO).build();
    }

    public Optional<StoredState> parse(String cookieValue) {
        if (cookieValue == null || cookieValue.isBlank()) {
            return Optional.empty();
        }
        String[] parts = cookieValue.split("\\.", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            String redirectUri = new String(Base64.getUrlDecoder().decode(parts[2]), StandardCharsets.UTF_8);
            return Optional.of(new StoredState(parts[0], parts[1], redirectUri));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(PATH);
    }

    public record StoredState(String state, String codeVerifier, String redirectUri) {
    }
}
