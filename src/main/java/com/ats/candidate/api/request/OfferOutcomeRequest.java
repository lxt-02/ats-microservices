package com.ats.candidate.api.request;

import com.ats.candidate.application.command.OfferOutcomeCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OfferOutcomeRequest(
        @NotNull(message = "Offer outcome is required") Boolean accepted,
        @Size(max = 2000, message = "Note must not exceed 2,000 characters") String note
) {

    public OfferOutcomeCommand toCommand(Long applicationId) {
        return new OfferOutcomeCommand(applicationId, accepted, note);
    }
}
