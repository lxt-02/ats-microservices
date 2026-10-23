package com.ats.candidate.application.command;

public record OfferOutcomeCommand(Long applicationId, boolean accepted, String note) {
}
