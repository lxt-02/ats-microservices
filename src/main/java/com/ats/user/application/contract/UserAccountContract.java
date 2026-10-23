package com.ats.user.application.contract;

import com.ats.user.application.response.UserResponse;

import java.util.Optional;

/**
 * What the Auth module may do with user accounts. Deleted users are never returned.
 * {@code provider} is an auth provider name such as {@code "GOOGLE"}.
 */
public interface UserAccountContract {

    /** Status name of an account that may sign in. */
    String ACTIVE_STATUS = "ACTIVE";

    Optional<UserResponse> findById(Long userId);

    Optional<UserResponse> findByEmail(String email);

    Optional<UserResponse> findByExternalIdentity(String provider, String subject);

    /** Self-registration of a candidate after an external provider has verified the email. */
    UserResponse registerExternalCandidate(String fullName, String email, String provider, String subject,
                                           String actor);

    /**
     * Links an external identity to an existing account (e.g. a recruiter pre-provisioned by an admin).
     *
     * @throws com.ats.shared.exception.ConflictException when the account already uses another sign-in method
     */
    UserResponse linkExternalIdentity(Long userId, String provider, String subject, String actor);
}
