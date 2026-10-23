package com.ats.organization.domain.exception;

import com.ats.shared.exception.RuleViolationException;

/** Department rules. */
public final class DepartmentRuleViolation {

    private DepartmentRuleViolation() {
    }

    /** R1: a department cannot be its own parent. */
    public static RuleViolationException cannotBeOwnParent() {
        return new RuleViolationException("DEPARTMENT_R1", "Department cannot be its own parent", true);
    }

    /** R2: a deleted department cannot be changed. */
    public static RuleViolationException deletedCannotBeChanged() {
        return new RuleViolationException("DEPARTMENT_R2", "Deleted department cannot be changed", false);
    }
}
