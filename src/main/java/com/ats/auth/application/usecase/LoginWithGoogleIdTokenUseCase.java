package com.ats.auth.application.usecase;

import com.ats.auth.application.port.out.GoogleIdentityProvider;
import com.ats.auth.application.port.out.GoogleIdentityProvider.GoogleIdentity;
import com.ats.auth.application.response.GoogleLoginResponse;
import com.ats.auth.application.service.GoogleAccountService;
import org.springframework.stereotype.Service;

/** Google Identity Services flow (One Tap / button): the frontend sends the Google ID token. */
@Service
public class LoginWithGoogleIdTokenUseCase {

    private final GoogleIdentityProvider googleIdentityProvider;
    private final GoogleAccountService googleAccountService;

    public LoginWithGoogleIdTokenUseCase(GoogleIdentityProvider googleIdentityProvider,
                                         GoogleAccountService googleAccountService) {
        this.googleIdentityProvider = googleIdentityProvider;
        this.googleAccountService = googleAccountService;
    }

    public GoogleLoginResponse execute(String idToken) {
        GoogleIdentity identity = googleIdentityProvider.verifyIdToken(
                Inputs.requireText(idToken, "Google ID token is required"));
        return googleAccountService.signIn(identity);
    }
}
