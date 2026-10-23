package com.ats.candidate.domain.exception;

import com.ats.shared.exception.RuleViolationException;

import java.util.Map;

/** CV and application rules. Messages follow the SRS system messages. */
public final class CandidateRuleViolation {

    private CandidateRuleViolation() {
    }

    /** CV_R1 (BRULE-08, MSG-FILE-002): only PDF and DOCX. */
    public static RuleViolationException unsupportedCvFormat() {
        return new RuleViolationException("CV_R1", "Invalid format. Only PDF and DOCX files are supported.", true);
    }

    /** CV_R2 (BRULE-08, MSG-FILE-001): at most 5 MB. */
    public static RuleViolationException cvTooLarge() {
        return new RuleViolationException("CV_R2",
                "File size exceeds the 5MB limit. Please upload a smaller file.", true);
    }

    /** CV_R3 (MSG-VAL-003): a CV file is required. */
    public static RuleViolationException cvRequired() {
        return new RuleViolationException("CV_R3", "Please upload your CV (PDF or DOCX, max 5MB).", true);
    }

    /** APPLICATION_R1 (BRULE-09, MSG-DUP-001): one application per email and job. */
    public static RuleViolationException duplicateApplication() {
        return new RuleViolationException("APPLICATION_R1",
                "You have already applied for this position with this email address.", false);
    }

    /** APPLICATION_R2 (BRULE-10): the job is closed, unpublished or past its deadline. */
    public static RuleViolationException jobNotAcceptingApplications(Long jobId) {
        return new RuleViolationException("APPLICATION_R2",
                "This position is no longer accepting applications.", false, Map.of("jobId", jobId));
    }
}
