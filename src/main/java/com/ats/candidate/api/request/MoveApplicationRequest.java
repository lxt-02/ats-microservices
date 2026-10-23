package com.ats.candidate.api.request;

import com.ats.candidate.application.command.MoveApplicationCommand;
import com.ats.candidate.domain.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MoveApplicationRequest(
        @NotNull(message = "Target stage is required") ApplicationStatus toStatus,
        @Size(max = 2000, message = "Note must not exceed 2,000 characters") String note
) {

    public MoveApplicationCommand toCommand(Long applicationId) {
        return new MoveApplicationCommand(applicationId, toStatus, note);
    }
}
