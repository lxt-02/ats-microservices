package com.ats.auth.application;

import com.ats.auth.application.AuthTestFixtures.FakeAccessTokenIssuer;
import com.ats.auth.application.AuthTestFixtures.InMemoryRefreshTokenRepository;
import com.ats.auth.application.AuthTestFixtures.InMemoryUserRepository;
import com.ats.auth.application.response.AuthTokenResponse;
import com.ats.auth.application.service.AuthSessionService;
import com.ats.auth.application.usecase.LogoutUseCase;
import com.ats.auth.application.usecase.RefreshSessionUseCase;
import com.ats.auth.domain.aggregate.RefreshToken;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.user.application.response.UserResponse;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.UserRole;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSessionTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemoryRefreshTokenRepository tokens = new InMemoryRefreshTokenRepository();
    private final Clock clock = Clock.systemUTC();
    private final AuthSessionService sessions = new AuthSessionService(tokens, new FakeAccessTokenIssuer(), 3600,
            clock);
    private final RefreshSessionUseCase refresh = new RefreshSessionUseCase(tokens,
            AuthTestFixtures.userAccounts(users), sessions, clock);
    private final LogoutUseCase logout = new LogoutUseCase(tokens, clock);

    private User activeUser() {
        return users.save(User.create("Recruiter", Email.of("r@ats.dev"), null, PhoneNumber.of(null),
                UserRole.RECRUITER, null, "test"));
    }

    @Test
    void openStoresOnlyTheHashOfTheRefreshToken() {
        AuthTokenResponse session = sessions.open(UserResponse.from(activeUser()));

        assertEquals(1, tokens.all().size());
        assertNotEquals(session.refreshToken(), tokens.all().get(0).getTokenHash());
        assertEquals(64, tokens.all().get(0).getTokenHash().length());
        assertEquals("Bearer", session.tokenType());
    }

    @Test
    void refreshRotatesTheToken() {
        AuthTokenResponse first = sessions.open(UserResponse.from(activeUser()));

        AuthTokenResponse second = refresh.execute(first.refreshToken());

        assertNotEquals(first.refreshToken(), second.refreshToken());
        RefreshToken old = tokens.all().get(0);
        assertEquals(RefreshToken.REASON_ROTATED, old.getRevokeReason());
        assertEquals(tokens.all().get(1).getId(), old.getReplacedById());
    }

    @Test
    void reusingARotatedTokenRevokesEverySessionOfTheUser() {
        AuthTokenResponse first = sessions.open(UserResponse.from(activeUser()));
        AuthTokenResponse second = refresh.execute(first.refreshToken());

        assertThrows(AuthenticationFailedException.class, () -> refresh.execute(first.refreshToken()));

        assertTrue(tokens.all().stream().allMatch(RefreshToken::isRevoked));
        assertThrows(AuthenticationFailedException.class, () -> refresh.execute(second.refreshToken()));
    }

    @Test
    void logoutRevokesTheTokenAndIsIdempotent() {
        AuthTokenResponse session = sessions.open(UserResponse.from(activeUser()));

        logout.execute(session.refreshToken());
        logout.execute(session.refreshToken());
        logout.execute("unknown-token");

        assertThrows(AuthenticationFailedException.class, () -> refresh.execute(session.refreshToken()));
        assertEquals(RefreshToken.REASON_LOGOUT, tokens.all().get(0).getRevokeReason());
    }

    @Test
    void refreshFailsWhenUserWasDeactivated() {
        User user = activeUser();
        AuthTokenResponse session = sessions.open(UserResponse.from(user));
        user.deactivate("admin");
        users.save(user);

        assertThrows(AuthenticationFailedException.class, () -> refresh.execute(session.refreshToken()));
    }

    @Test
    void unknownOrBlankTokenIsRejected() {
        assertThrows(AuthenticationFailedException.class, () -> refresh.execute("nope"));
        assertThrows(AuthenticationFailedException.class, () -> refresh.execute(" "));
    }

    @Test
    void cannotOpenSessionForInactiveUser() {
        User user = activeUser();
        user.lock("admin");

        assertThrows(AuthenticationFailedException.class, () -> sessions.open(UserResponse.from(user)));
    }
}
