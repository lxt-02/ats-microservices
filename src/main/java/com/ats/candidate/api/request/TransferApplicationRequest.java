package com.ats.candidate.api.request;

import com.ats.candidate.application.command.TransferApplicationCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransferApplicationRequest(
        @NotNull(message = "Target job is required") Long targetJobId,
        @Size(max = 2000, message = "Note must not exceed 2,000 characters") String note
) {

    public TransferApplicationCommand toCommand(Long applicationId) {
        return new TransferApplicationCommand(applicationId, targetJobId, note);
    }
}
