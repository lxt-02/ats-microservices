package com.ats.auth.application;

import com.ats.auth.application.AuthTestFixtures.FakeAccessTokenIssuer;
import com.ats.auth.application.AuthTestFixtures.FakeGoogleIdentityProvider;
import com.ats.auth.application.AuthTestFixtures.InMemoryRefreshTokenRepository;
import com.ats.auth.application.AuthTestFixtures.InMemoryUserRepository;
import com.ats.auth.application.command.GoogleCallbackCommand;
import com.ats.auth.application.port.out.GoogleIdentityProvider.GoogleIdentity;
import com.ats.auth.application.response.GoogleAuthorizationResponse;
import com.ats.auth.application.response.GoogleLoginResponse;
import com.ats.auth.application.service.AuthSessionService;
import com.ats.auth.application.service.GoogleAccountService;
import com.ats.auth.application.usecase.CompleteGoogleAuthorizationUseCase;
import com.ats.auth.application.usecase.LoginWithGoogleIdTokenUseCase;
import com.ats.auth.application.usecase.StartGoogleAuthorizationUseCase;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.exception.ConflictException;
import com.ats.shared.security.SecureTokens;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.AuthProvider;
import com.ats.user.domain.enums.UserRole;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleLoginTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final FakeGoogleIdentityProvider google = new FakeGoogleIdentityProvider();
    private final StartGoogleAuthorizationUseCase start = new StartGoogleAuthorizationUseCase(google);

    private GoogleAccountService accounts(boolean autoRegister) {
        AuthSessionService sessions = new AuthSessionService(new InMemoryRefreshTokenRepository(),
                new FakeAccessTokenIssuer(), 3600, Clock.systemUTC());
        return new GoogleAccountService(AuthTestFixtures.userAccounts(users), sessions, autoRegister);
    }

    private CompleteGoogleAuthorizationUseCase callback(boolean autoRegister) {
        return new CompleteGoogleAuthorizationUseCase(google, accounts(autoRegister));
    }

    private LoginWithGoogleIdTokenUseCase idTokenLogin(boolean autoRegister) {
        return new LoginWithGoogleIdTokenUseCase(google, accounts(autoRegister));
    }

    @Test
    void startAuthorizationUsesPkceS256() {
        GoogleAuthorizationResponse request = start.execute(null);

        assertEquals(FakeGoogleIdentityProvider.DEFAULT_REDIRECT, request.redirectUri());
        assertEquals(SecureTokens.pkceChallenge(request.codeVerifier()), google.lastChallenge);
        assertTrue(request.codeVerifier().length() >= 43);
    }

    @Test
    void startAuthorizationRejectsRedirectUriOutsideWhitelist() {
        assertThrows(IllegalArgumentException.class, () -> start.execute("https://evil.example"));
    }

    @Test
    void callbackWithWrongStateIsRejectedBeforeCallingGoogle() {
        CompleteGoogleAuthorizationUseCase callback = callback(true);
        GoogleAuthorizationResponse request = start.execute(null);

        assertThrows(AuthenticationFailedException.class, () -> callback.execute(new GoogleCallbackCommand(
                "code", "attacker-state", request.state(), request.codeVerifier(), request.redirectUri())));
        assertThrows(AuthenticationFailedException.class, () -> callback.execute(new GoogleCallbackCommand(
                "code", request.state(), null, request.codeVerifier(), request.redirectUri())));
        assertEquals(0, google.exchangeCalls);
    }

    @Test
    void callbackWithMatchingStateRegistersCandidateAndSendsVerifier() {
        GoogleAuthorizationResponse request = start.execute(null);

        GoogleLoginResponse result = callback(true).execute(new GoogleCallbackCommand(
                "code", request.state(), request.state(), request.codeVerifier(), request.redirectUri()));

        assertTrue(result.newUser());
        assertEquals("CANDIDATE", result.user().role());
        assertEquals("GOOGLE", result.user().authProvider());
        assertEquals(request.codeVerifier(), google.lastCodeVerifier);
        assertNotNull(result.tokens().refreshToken());
    }

    @Test
    void secondLoginFindsTheLinkedUser() {
        idTokenLogin(true).execute("id-token");

        GoogleLoginResponse second = idTokenLogin(true).execute("id-token");

        assertFalse(second.newUser());
        assertEquals(1, users.count());
    }

    @Test
    void preProvisionedAccountWithoutPasswordIsLinkedAndKeepsItsRole() {
        users.save(User.create("Recruiter", Email.of("user@gmail.com"), null, PhoneNumber.of(null),
                UserRole.RECRUITER, null, "admin"));

        GoogleLoginResponse result = idTokenLogin(false).execute("id-token");

        assertFalse(result.newUser());
        assertEquals("RECRUITER", result.user().role());
        assertEquals("google-sub-1", result.user().externalSubjectId());
        assertEquals(1, users.count());
    }

    @Test
    void accountWithPasswordIsNotTakenOverByGoogleLogin() {
        users.save(User.create("Local", Email.of("user@gmail.com"), "$2a$10$hash", PhoneNumber.of(null),
                UserRole.ADMIN, null, "admin"));

        assertThrows(ConflictException.class, () -> idTokenLogin(true).execute("id-token"));
    }

    @Test
    void unknownUserIsRejectedWhenAutoRegisterIsOff() {
        assertThrows(AuthenticationFailedException.class, () -> idTokenLogin(false).execute("id-token"));
        assertEquals(0, users.count());
    }

    @Test
    void inactiveLinkedUserCannotSignIn() {
        User user = users.save(User.createExternal("A", Email.of("user@gmail.com"), PhoneNumber.of(null),
                UserRole.CANDIDATE, null, AuthProvider.GOOGLE, "google-sub-1", "test"));
        user.lock("admin");
        users.save(user);

        assertThrows(AuthenticationFailedException.class, () -> idTokenLogin(true).execute("id-token"));
    }

    @Test
    void displayNameFallsBackToEmail() {
        assertEquals("a@b.c", new GoogleIdentity("s", "a@b.c", " ", null).displayName());
    }
}
