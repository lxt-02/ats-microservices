package com.ats.shared.security;

import java.util.Set;

/**
 * The authenticated caller, built from the verified ATS access token. Controllers receive it as a method
 * parameter; application and domain code never see JWTs or HTTP headers.
 */
public record CurrentUser(Long userId, Set<String> roles) {

    /** Request attribute under which the authentication filter stores the current user. */
    public static final String REQUEST_ATTRIBUTE = CurrentUser.class.getName();

    public CurrentUser {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /** Value written to created_by / updated_by. */
    public String actor() {
        return String.valueOf(userId);
    }
}
