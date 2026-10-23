package com.ats.shared.security;

/** Role names carried in access tokens (same names as the User module's roles). */
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String RECRUITER = "RECRUITER";
    public static final String HIRING_MANAGER = "HIRING_MANAGER";
    public static final String CANDIDATE = "CANDIDATE";

    private Roles() {
    }
}
