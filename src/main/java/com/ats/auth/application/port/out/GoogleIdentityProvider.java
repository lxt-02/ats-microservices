package com.ats.auth.application.port.out;

import java.net.URI;

public interface GoogleIdentityProvider {

    /**
     * Returns the redirect URI to use for this login. A requested URI must be in the configured whitelist;
     * when none is requested, the default redirect URI is returned.
     */
    String resolveRedirectUri(String requestedRedirectUri);

    URI buildAuthorizationUri(String state, String codeChallenge, String redirectUri);

    /**
     * Exchanges an authorization code at Google's token endpoint and verifies the returned ID token.
     *
     * @param codeVerifier PKCE verifier, may be null when the client did not use PKCE
     */
    GoogleIdentity exchangeAuthorizationCode(String code, String codeVerifier, String redirectUri);

    /**
     * Verifies a Google ID token (signature via Google JWKS, issuer, audience, expiry, verified email).
     */
    GoogleIdentity verifyIdToken(String idToken);

    record GoogleIdentity(String subject, String email, String name, String pictureUrl) {

        public String displayName() {
            return name == null || name.isBlank() ? email : name.trim();
        }
    }
}
