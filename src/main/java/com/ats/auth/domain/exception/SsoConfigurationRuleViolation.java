package com.ats.auth.domain.exception;

import com.ats.shared.exception.RuleViolationException;

/** SSO configuration rules. */
public final class SsoConfigurationRuleViolation {

    private SsoConfigurationRuleViolation() {
    }

    /** R1: a deleted configuration cannot be changed. */
    public static RuleViolationException deletedCannotBeChanged() {
        return new RuleViolationException("SSO_R1", "Deleted SSO configuration cannot be changed", false);
    }

    /** R2: only KEYCLOAK and GOOGLE are supported. */
    public static RuleViolationException unsupportedProvider() {
        return new RuleViolationException("SSO_R2", "Unsupported SSO provider type", true);
    }
}
