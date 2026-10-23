package com.ats.candidate.application.command;

/** {@code approve}: Offered with {@code teamId}; otherwise Rejected with {@code comment} as reason. */
public record HmDecisionCommand(Long applicationId, boolean approve, Long teamId, String comment) {
}
