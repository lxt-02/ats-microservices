package com.ats.auth.application.command;

/**
 * @param code          authorization code returned by Google
 * @param returnedState state returned by Google in the callback query string
 * @param expectedState state stored in the browser cookie when the login started
 * @param codeVerifier  PKCE verifier stored with the state
 * @param redirectUri   redirect URI used when the login started
 */
public record GoogleCallbackCommand(
        String code,
        String returnedState,
        String expectedState,
        String codeVerifier,
        String redirectUri
) {
}
