package com.ats.shared.exception;

import java.util.Map;

/**
 * A domain rule (R1, R2...) was violated. Raised by the {@code <Entity>RuleViolation} factories of each module.
 * {@code invalidInput = true} means the caller sent bad data (HTTP 400); otherwise the aggregate's current state
 * refuses the change (HTTP 409).
 */
public class RuleViolationException extends ApplicationException {

    private final String rule;
    private final boolean invalidInput;
    private final Map<String, Object> details;

    public RuleViolationException(String rule, String message, boolean invalidInput) {
        this(rule, message, invalidInput, Map.of());
    }

    public RuleViolationException(String rule, String message, boolean invalidInput, Map<String, Object> details) {
        super(message);
        this.rule = rule;
        this.invalidInput = invalidInput;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public String getRule() {
        return rule;
    }

    public boolean isInvalidInput() {
        return invalidInput;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
