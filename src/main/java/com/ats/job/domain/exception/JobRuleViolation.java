package com.ats.job.domain.exception;

import com.ats.job.domain.enums.JobStatus;
import com.ats.shared.exception.RuleViolationException;

import java.util.List;
import java.util.Map;

/** Job rules. Messages follow the SRS system messages (MSG-JOB-xxx). */
public final class JobRuleViolation {

    private JobRuleViolation() {
    }

    /** R1 (MSG-JOB-001): required fields are missing before submitting or publishing. */
    public static RuleViolationException missingRequiredFields(List<String> fields) {
        return new RuleViolationException("JOB_R1",
                "Please fill in all required fields marked with (*) before submitting.", true,
                Map.of("fields", List.copyOf(fields)));
    }

    /** R2 (MSG-JOB-002, BRULE-14): maximum salary is lower than minimum salary. */
    public static RuleViolationException salaryRangeInvalid() {
        return new RuleViolationException("JOB_R2",
                "Maximum salary must be greater than or equal to minimum salary.", true);
    }

    /** R3 (MSG-JOB-003): the application deadline is in the past. */
    public static RuleViolationException deadlineInPast() {
        return new RuleViolationException("JOB_R3", "Application deadline must be a future date.", true);
    }

    /** R4 (BRULE-13): title, department and employment type are read-only while published. */
    public static RuleViolationException criticalFieldsLocked() {
        return new RuleViolationException("JOB_R4",
                "Job title, department and employment type cannot be changed while the job is published. "
                        + "Unpublish the job first.", false);
    }

    /** R5 (BRULE-11): the Hiring Manager must approve before publishing. */
    public static RuleViolationException approvalRequired() {
        return new RuleViolationException("JOB_R5",
                "This job requires Hiring Manager approval before it can be published.", false);
    }

    /** R6: the action is not allowed in the current status. */
    public static RuleViolationException actionNotAllowed(String action, JobStatus status) {
        return new RuleViolationException("JOB_R6",
                "Cannot " + action + " a job in status " + status + ".", false,
                Map.of("status", status.name()));
    }

    /** R7: only drafts without applications can be deleted. */
    public static RuleViolationException cannotDelete() {
        return new RuleViolationException("JOB_R7",
                "Only draft jobs without applications can be deleted.", false);
    }

    /** R8 (BRULE-11): a rejection needs a reason. */
    public static RuleViolationException rejectionReasonRequired() {
        return new RuleViolationException("JOB_R8", "A rejection reason is required.", true);
    }
}
