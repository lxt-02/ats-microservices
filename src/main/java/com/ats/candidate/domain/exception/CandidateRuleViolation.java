package com.ats.candidate.domain.exception;

import com.ats.shared.exception.RuleViolationException;

import com.ats.candidate.domain.enums.ApplicationStatus;

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

    /** APPLICATION_R3 (BRULE-12): the pipeline move is not allowed from the current state. */
    public static RuleViolationException invalidTransition(ApplicationStatus from, ApplicationStatus to) {
        return new RuleViolationException("APPLICATION_R3",
                "Cannot move the candidate from " + from + " to " + to + ".", false,
                Map.of("from", from.name(), "to", to.name(),
                        "allowed", from.next().stream().map(Enum::name).sorted().toList()));
    }

    /** APPLICATION_R4 (BRULE-04, MSG-PIPE-002): transfer only during CV screening. */
    public static RuleViolationException transferNotAllowed() {
        return new RuleViolationException("APPLICATION_R4",
                "Transfer is only allowed during the CV Screening stage (Round 1). "
                        + "This candidate has already progressed beyond Round 1.", false);
    }

    /** APPLICATION_R5: a rejection needs a reason. */
    public static RuleViolationException reasonRequired() {
        return new RuleViolationException("APPLICATION_R5", "A reason is required.", true);
    }

    /** APPLICATION_R6 (UC-03.3): approving needs a target team/unit. */
    public static RuleViolationException targetTeamRequired() {
        return new RuleViolationException("APPLICATION_R6", "Choose the target team/unit for the candidate.", true);
    }

    /** APPLICATION_R7: only rejected or declined applications can be reopened, back to their previous stage. */
    public static RuleViolationException reopenNotAllowed(ApplicationStatus status) {
        return new RuleViolationException("APPLICATION_R7",
                "Only rejected or declined applications can be reopened.", false, Map.of("status", status.name()));
    }

    /** APPLICATION_R8: transfer target must be another job that accepts applications. */
    public static RuleViolationException invalidTransferTarget() {
        return new RuleViolationException("APPLICATION_R8",
                "The target job must be another published job that accepts applications.", true);
    }
}
