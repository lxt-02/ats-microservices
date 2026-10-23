package com.ats.auth.infrastructure.security;

import com.ats.auth.application.port.out.AccessTokenIssuer;
import com.ats.auth.infrastructure.configuration.JwtProperties;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Issues and verifies ATS access tokens (HS256 JWT). Refresh tokens are opaque and handled by the
 * application layer. Verification replaces the former API Gateway: signature, issuer, type and expiry.
 */
@Service
public class JwtTokenService implements AccessTokenIssuer {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String ACCESS_TYPE = "access";
    private static final int MIN_SECRET_LENGTH = 32;
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {
    };

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;
    private final SecretKeySpec signingKey;
    private final Clock clock;

    public JwtTokenService(JwtProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.signingKey = createSigningKey(properties.getSecret());
    }

    @Override
    public IssuedAccessToken issue(Long userId, List<String> roles) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        Instant issuedAt = clock.instant();
        long ttlSeconds = properties.getAccessTokenTtlSeconds();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", properties.getIssuer());
        claims.put("sub", userId.toString());
        claims.put("roles", roles == null ? List.of() : List.copyOf(roles));
        claims.put("typ", ACCESS_TYPE);
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", issuedAt.plusSeconds(ttlSeconds).getEpochSecond());

        String signingInput = base64UrlJson(header) + "." + base64UrlJson(claims);
        return new IssuedAccessToken(signingInput + "." + sign(signingInput), ttlSeconds);
    }

    /**
     * Verifies an access token and returns the caller it identifies.
     *
     * @throws AuthenticationFailedException when the token is malformed, forged, expired or not an access token
     */
    public CurrentUser verifyAccessToken(String token) {
        if (token == null || token.isBlank()) {
            throw invalid("Token is required");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw invalid("Token format is invalid");
        }

        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw invalid("Token signature is invalid");
        }

        Map<String, Object> claims;
        try {
            claims = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), CLAIMS_TYPE);
        } catch (RuntimeException ex) {
            throw invalid("Token payload is invalid");
        }
        if (!properties.getIssuer().equals(claims.get("iss"))) {
            throw invalid("Token issuer is invalid");
        }
        if (!ACCESS_TYPE.equals(claims.get("typ"))) {
            throw invalid("Token is not an access token");
        }
        if (!(claims.get("exp") instanceof Number expiresAt)
                || !Instant.ofEpochSecond(expiresAt.longValue()).isAfter(clock.instant())) {
            throw invalid("Token is expired");
        }
        return new CurrentUser(userId(claims.get("sub")), roles(claims.get("roles")));
    }

    private static Long userId(Object subject) {
        try {
            long value = Long.parseLong(String.valueOf(subject));
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        throw invalid("Token subject is invalid");
    }

    private static Set<String> roles(Object value) {
        Set<String> roles = new LinkedHashSet<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !item.toString().isBlank()) {
                    roles.add(item.toString().trim());
                }
            }
        } else if (value instanceof String text && !text.isBlank()) {
            for (String item : text.split(",")) {
                if (!item.isBlank()) {
                    roles.add(item.trim());
                }
            }
        }
        return roles;
    }

    private String base64UrlJson(Map<String, Object> value) {
        return BASE64_URL.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);
            return BASE64_URL.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not sign JWT", ex);
        }
    }

    private static AuthenticationFailedException invalid(String message) {
        return new AuthenticationFailedException(message);
    }

    private static SecretKeySpec createSigningKey(String secret) {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("ats.security.jwt.secret must be at least " + MIN_SECRET_LENGTH
                    + " characters");
        }
        if (secret.startsWith("dev-only")) {
            log.warn("Using the development JWT secret. Set ATS_JWT_SECRET before deploying.");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }
}
