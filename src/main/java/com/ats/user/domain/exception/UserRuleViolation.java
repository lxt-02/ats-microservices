package com.ats.user.domain.exception;

import com.ats.shared.exception.RuleViolationException;

/** User rules. */
public final class UserRuleViolation {

    private UserRuleViolation() {
    }

    /** R1: a deleted user cannot be changed. */
    public static RuleViolationException deletedUserCannotBeChanged() {
        return new RuleViolationException("USER_R1", "Deleted user cannot be changed", false);
    }

    /** R2: an account signing in through an external provider must carry the provider's subject id. */
    public static RuleViolationException externalSubjectRequired() {
        return new RuleViolationException("USER_R2",
                "External subject id is required for external auth provider", true);
    }
}
