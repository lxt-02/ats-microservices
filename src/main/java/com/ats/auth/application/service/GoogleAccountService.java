package com.ats.auth.application.service;

import com.ats.auth.application.port.out.GoogleIdentityProvider.GoogleIdentity;
import com.ats.auth.application.response.GoogleLoginResponse;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.user.application.contract.UserAccountContract;
import com.ats.user.application.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Maps a verified Google identity to an ATS user (find, link or register) and opens a session.
 * Runs in one DB transaction; all calls to Google happen before this, outside the transaction.
 */
@Service
public class GoogleAccountService {

    static final String GOOGLE = "GOOGLE";
    private static final String SYSTEM_ACTOR = "google-oauth";

    private final UserAccountContract userAccounts;
    private final AuthSessionService authSessionService;
    private final boolean autoRegisterCandidates;

    public GoogleAccountService(UserAccountContract userAccounts,
                                AuthSessionService authSessionService,
                                @Value("${google.oauth.auto-register-candidates:true}") boolean autoRegisterCandidates) {
        this.userAccounts = userAccounts;
        this.authSessionService = authSessionService;
        this.autoRegisterCandidates = autoRegisterCandidates;
    }

    @Transactional
    public GoogleLoginResponse signIn(GoogleIdentity identity) {
        Optional<UserResponse> linkedUser = userAccounts.findByExternalIdentity(GOOGLE, identity.subject());
        if (linkedUser.isPresent()) {
            return openSession(linkedUser.get(), false);
        }

        Optional<UserResponse> userWithSameEmail = userAccounts.findByEmail(identity.email());
        if (userWithSameEmail.isPresent()) {
            // A pre-provisioned account (same email, no password) is linked on first login.
            // Google has verified the email at this point.
            AuthSessionService.ensureActive(userWithSameEmail.get());
            UserResponse linked = userAccounts.linkExternalIdentity(userWithSameEmail.get().id(), GOOGLE,
                    identity.subject(), SYSTEM_ACTOR);
            return openSession(linked, false);
        }

        if (!autoRegisterCandidates) {
            throw new AuthenticationFailedException("No ATS account is associated with this Google account");
        }
        UserResponse candidate = userAccounts.registerExternalCandidate(identity.displayName(), identity.email(),
                GOOGLE, identity.subject(), SYSTEM_ACTOR);
        return openSession(candidate, true);
    }

    private GoogleLoginResponse openSession(UserResponse user, boolean newUser) {
        return new GoogleLoginResponse(user, authSessionService.open(user), newUser);
    }
}
