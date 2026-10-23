package com.ats.auth.application.response;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds
) {

    public static final String BEARER = "Bearer";
}
