package com.ats.auth.application.response;

import com.ats.user.application.response.UserResponse;

/** {@code newUser} is true when this login created a new candidate account. */
public record GoogleLoginResponse(UserResponse user, AuthTokenResponse tokens, boolean newUser) {
}
