package com.ats.auth.application.port.out;

import java.util.List;

/** Signs ATS access tokens. */
public interface AccessTokenIssuer {

    IssuedAccessToken issue(Long userId, List<String> roles);

    record IssuedAccessToken(String token, long expiresInSeconds) {
    }
}
