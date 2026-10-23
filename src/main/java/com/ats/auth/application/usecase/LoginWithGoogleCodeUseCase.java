package com.ats.auth.application.usecase;

import com.ats.auth.application.command.GoogleCodeLoginCommand;
import com.ats.auth.application.port.out.GoogleIdentityProvider;
import com.ats.auth.application.port.out.GoogleIdentityProvider.GoogleIdentity;
import com.ats.auth.application.response.GoogleLoginResponse;
import com.ats.auth.application.service.GoogleAccountService;
import org.springframework.stereotype.Service;

/** SPA flow: the frontend ran the redirect itself (and checked state) and sends the code. */
@Service
public class LoginWithGoogleCodeUseCase {

    private final GoogleIdentityProvider googleIdentityProvider;
    private final GoogleAccountService googleAccountService;

    public LoginWithGoogleCodeUseCase(GoogleIdentityProvider googleIdentityProvider,
                                      GoogleAccountService googleAccountService) {
        this.googleIdentityProvider = googleIdentityProvider;
        this.googleAccountService = googleAccountService;
    }

    public GoogleLoginResponse execute(GoogleCodeLoginCommand command) {
        String redirectUri = googleIdentityProvider.resolveRedirectUri(command.redirectUri());
        GoogleIdentity identity = googleIdentityProvider.exchangeAuthorizationCode(
                Inputs.requireText(command.code(), "Authorization code is required"),
                Inputs.blankToNull(command.codeVerifier()), redirectUri);
        return googleAccountService.signIn(identity);
    }
}
