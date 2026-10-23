package com.ats.auth.application.response;

import java.net.URI;

/**
 * Everything the web layer needs to start a Google login: where to redirect the browser,
 * and the values it must keep (in an HttpOnly cookie) to verify the callback.
 */
public record GoogleAuthorizationResponse(
        URI authorizationUri,
        String state,
        String codeVerifier,
        String redirectUri
) {
}
