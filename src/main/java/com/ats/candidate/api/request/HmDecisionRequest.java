package com.ats.candidate.api.request;

import com.ats.candidate.application.command.HmDecisionCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code APPROVE} with {@code teamId} (department), or {@code REJECT} with {@code comment} as reason. */
public record HmDecisionRequest(
        @NotNull(message = "Decision is required") Decision decision,
        Long teamId,
        @Size(max = 2000, message = "Comment must not exceed 2,000 characters") String comment
) {

    public enum Decision {
        APPROVE,
        REJECT
    }

    public HmDecisionCommand toCommand(Long applicationId) {
        return new HmDecisionCommand(applicationId, decision == Decision.APPROVE, teamId, comment);
    }
}
