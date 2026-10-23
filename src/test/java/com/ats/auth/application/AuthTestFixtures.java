package com.ats.auth.application;

import com.ats.auth.application.port.out.AccessTokenIssuer;
import com.ats.auth.application.port.out.GoogleIdentityProvider;
import com.ats.auth.domain.aggregate.RefreshToken;
import com.ats.auth.domain.repository.RefreshTokenRepository;
import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.user.application.service.UserAccountService;
import com.ats.user.application.service.UserLoader;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.AuthProvider;
import com.ats.user.domain.repository.UserRepository;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.UserId;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory fakes so the auth flow can be unit-tested without Spring, a database or Google. */
final class AuthTestFixtures {

    private AuthTestFixtures() {
    }

    /** The real User module contract implementation over an in-memory repository. */
    static UserAccountService userAccounts(InMemoryUserRepository users) {
        return new UserAccountService(users, new UserLoader(users, new DepartmentDirectory() {
            @Override
            public boolean exists(Long departmentId) {
                return true;
            }

            @Override
            public Map<Long, String> findNames(Collection<Long> departmentIds) {
                return Map.of();
            }
        }));
    }

    static final class InMemoryUserRepository implements UserRepository {

        private final Map<Long, User> users = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public User save(User user) {
            Long id = user.getId() == null ? sequence.incrementAndGet() : user.getId().value();
            User stored = User.restore(UserId.of(id), user.getDepartmentId(), user.getFullName(), user.getEmail(),
                    user.getPasswordHash(), user.getPhoneNumber(), user.getRole(), user.getAuthProvider(),
                    user.getExternalSubjectId(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt(),
                    user.getCreatedBy(), user.getUpdatedBy(), user.isDeleted(), user.getDeletedAt());
            users.put(id, stored);
            return stored;
        }

        @Override
        public Optional<User> findById(UserId id) {
            return Optional.ofNullable(users.get(id.value())).filter(user -> !user.isDeleted());
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return users.values().stream()
                    .filter(user -> !user.isDeleted() && user.getEmail().equals(email))
                    .findFirst();
        }

        @Override
        public Optional<User> findByAuthProviderAndExternalSubjectId(AuthProvider provider, String subject) {
            return users.values().stream()
                    .filter(user -> !user.isDeleted() && user.getAuthProvider() == provider
                            && Objects.equals(user.getExternalSubjectId(), subject))
                    .findFirst();
        }

        @Override
        public boolean existsByEmail(Email email) {
            return findByEmail(email).isPresent();
        }

        int count() {
            return users.size();
        }
    }

    static final class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

        private final Map<Long, RefreshToken> tokens = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public RefreshToken save(RefreshToken token) {
            Long id = token.getId() == null ? sequence.incrementAndGet() : token.getId();
            RefreshToken stored = copy(id, token);
            tokens.put(id, stored);
            return copy(id, stored);
        }

        @Override
        public Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash) {
            return tokens.values().stream()
                    .filter(token -> token.getTokenHash().equals(tokenHash))
                    .findFirst()
                    .map(token -> copy(token.getId(), token));
        }

        @Override
        public int revokeAllActiveByUserId(Long userId, String reason, Instant now) {
            int revoked = 0;
            for (RefreshToken token : tokens.values()) {
                if (token.getUserId().equals(userId) && !token.isRevoked()) {
                    token.revoke(reason, now);
                    revoked++;
                }
            }
            return revoked;
        }

        List<RefreshToken> all() {
            return new ArrayList<>(tokens.values());
        }

        private static RefreshToken copy(Long id, RefreshToken t) {
            return RefreshToken.restore(id, t.getUserId(), t.getTokenHash(), t.getCreatedAt(), t.getExpiresAt(),
                    t.getRevokedAt(), t.getRevokeReason(), t.getReplacedById());
        }
    }

    static final class FakeAccessTokenIssuer implements AccessTokenIssuer {

        private final AtomicLong counter = new AtomicLong();

        @Override
        public IssuedAccessToken issue(Long userId, List<String> roles) {
            return new IssuedAccessToken("access-" + userId + "-" + counter.incrementAndGet(), 900);
        }
    }

    static final class FakeGoogleIdentityProvider implements GoogleIdentityProvider {

        static final String DEFAULT_REDIRECT = "http://localhost:8080/api/v1/auth/google/callback";

        GoogleIdentity identity = new GoogleIdentity("google-sub-1", "user@gmail.com", "Nguyen Van A", null);
        int exchangeCalls;
        String lastCodeVerifier;
        String lastChallenge;

        @Override
        public String resolveRedirectUri(String requested) {
            if (requested == null || requested.isBlank()) {
                return DEFAULT_REDIRECT;
            }
            if (!DEFAULT_REDIRECT.equals(requested)) {
                throw new IllegalArgumentException("Redirect URI is not allowed");
            }
            return requested;
        }

        @Override
        public URI buildAuthorizationUri(String state, String codeChallenge, String redirectUri) {
            lastChallenge = codeChallenge;
            return URI.create("https://accounts.google.com/o/oauth2/v2/auth?state=" + state);
        }

        @Override
        public GoogleIdentity exchangeAuthorizationCode(String code, String codeVerifier, String redirectUri) {
            exchangeCalls++;
            lastCodeVerifier = codeVerifier;
            return identity;
        }

        @Override
        public GoogleIdentity verifyIdToken(String idToken) {
            return identity;
        }
    }
}
