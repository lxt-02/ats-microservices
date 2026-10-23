package com.ats.auth.infrastructure.security;

import com.ats.auth.infrastructure.configuration.JwtProperties;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.security.CurrentUser;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-01T00:00:00Z");

    private static JwtTokenService service(String secret, String issuer, Clock clock) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setIssuer(issuer);
        properties.setAccessTokenTtlSeconds(900);
        return new JwtTokenService(properties, JsonMapper.builder().build(), clock);
    }

    private final JwtTokenService service = service("test-only-jwt-secret-with-at-least-32-characters", "ats",
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void issuedTokenVerifiesToTheSameUser() {
        String token = service.issue(125L, List.of("RECRUITER")).token();

        CurrentUser user = service.verifyAccessToken(token);

        assertEquals(125L, user.userId());
        assertEquals(Set.of("RECRUITER"), user.roles());
    }

    @Test
    void tamperedPayloadIsRejected() {
        String[] parts = service.issue(125L, List.of("RECRUITER")).token().split("\\.");
        String forged = service.issue(1L, List.of("ADMIN")).token().split("\\.")[1];

        assertThrows(AuthenticationFailedException.class,
                () -> service.verifyAccessToken(parts[0] + "." + forged + "." + parts[2]));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtTokenService other = service("another-secret-with-at-least-32-characters!!", "ats",
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(AuthenticationFailedException.class,
                () -> service.verifyAccessToken(other.issue(125L, List.of("ADMIN")).token()));
    }

    @Test
    void expiredTokenIsRejected() {
        String token = service.issue(125L, List.of("RECRUITER")).token();
        JwtTokenService later = service("test-only-jwt-secret-with-at-least-32-characters", "ats",
                Clock.fixed(NOW.plus(Duration.ofMinutes(16)), ZoneOffset.UTC));

        assertThrows(AuthenticationFailedException.class, () -> later.verifyAccessToken(token));
    }

    @Test
    void wrongIssuerAndGarbageAreRejected() {
        JwtTokenService otherIssuer = service("test-only-jwt-secret-with-at-least-32-characters", "someone-else",
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(AuthenticationFailedException.class,
                () -> service.verifyAccessToken(otherIssuer.issue(1L, List.of()).token()));
        assertThrows(AuthenticationFailedException.class, () -> service.verifyAccessToken("abc"));
        assertThrows(AuthenticationFailedException.class, () -> service.verifyAccessToken(""));
    }
}
