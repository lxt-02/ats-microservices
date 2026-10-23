package com.ats.auth.application.usecase;

import com.ats.auth.application.port.out.GoogleIdentityProvider;
import com.ats.auth.application.response.GoogleAuthorizationResponse;
import com.ats.shared.security.SecureTokens;
import org.springframework.stereotype.Service;

/** Server-side redirect flow, step 1: build the Google consent URL with state + PKCE. */
@Service
public class StartGoogleAuthorizationUseCase {

    private static final int STATE_BYTES = 32;
    private static final int CODE_VERIFIER_BYTES = 48; // 64 chars, RFC 7636 allows 43-128

    private final GoogleIdentityProvider googleIdentityProvider;

    public StartGoogleAuthorizationUseCase(GoogleIdentityProvider googleIdentityProvider) {
        this.googleIdentityProvider = googleIdentityProvider;
    }

    public GoogleAuthorizationResponse execute(String requestedRedirectUri) {
        String redirectUri = googleIdentityProvider.resolveRedirectUri(requestedRedirectUri);
        String state = SecureTokens.randomUrlSafe(STATE_BYTES);
        String codeVerifier = SecureTokens.randomUrlSafe(CODE_VERIFIER_BYTES);
        return new GoogleAuthorizationResponse(
                googleIdentityProvider.buildAuthorizationUri(state, SecureTokens.pkceChallenge(codeVerifier),
                        redirectUri),
                state,
                codeVerifier,
                redirectUri
        );
    }
}
