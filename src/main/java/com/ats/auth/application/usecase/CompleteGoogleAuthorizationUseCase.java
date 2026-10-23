package com.ats.auth.application.usecase;

import com.ats.auth.application.command.GoogleCallbackCommand;
import com.ats.auth.application.port.out.GoogleIdentityProvider;
import com.ats.auth.application.port.out.GoogleIdentityProvider.GoogleIdentity;
import com.ats.auth.application.response.GoogleLoginResponse;
import com.ats.auth.application.service.GoogleAccountService;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.security.SecureTokens;
import org.springframework.stereotype.Service;

/**
 * Server-side redirect flow, step 2: verify state, exchange the code and sign the user in.
 * Deliberately NOT transactional: Google is called first, only {@link GoogleAccountService} holds a transaction.
 */
@Service
public class CompleteGoogleAuthorizationUseCase {

    private final GoogleIdentityProvider googleIdentityProvider;
    private final GoogleAccountService googleAccountService;

    public CompleteGoogleAuthorizationUseCase(GoogleIdentityProvider googleIdentityProvider,
                                              GoogleAccountService googleAccountService) {
        this.googleIdentityProvider = googleIdentityProvider;
        this.googleAccountService = googleAccountService;
    }

    public GoogleLoginResponse execute(GoogleCallbackCommand command) {
        String code = Inputs.requireText(command.code(), "Authorization code is required");
        if (!SecureTokens.constantTimeEquals(command.returnedState(), command.expectedState())) {
            throw new AuthenticationFailedException("OAuth state is invalid or the login session has expired");
        }
        String codeVerifier = Inputs.requireText(command.codeVerifier(), "OAuth login session has expired");
        String redirectUri = googleIdentityProvider.resolveRedirectUri(command.redirectUri());

        GoogleIdentity identity = googleIdentityProvider.exchangeAuthorizationCode(code, codeVerifier, redirectUri);
        return googleAccountService.signIn(identity);
    }
}
