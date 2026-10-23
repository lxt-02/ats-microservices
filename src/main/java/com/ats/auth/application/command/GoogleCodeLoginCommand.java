package com.ats.auth.application.command;

/**
 * SPA flow: the frontend ran the Google redirect itself (and checked state) and sends the code.
 *
 * @param codeVerifier PKCE verifier, when the frontend started the flow with a code_challenge
 */
public record GoogleCodeLoginCommand(String code, String codeVerifier, String redirectUri) {
}
