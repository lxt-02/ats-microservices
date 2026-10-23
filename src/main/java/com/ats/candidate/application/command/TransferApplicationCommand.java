package com.ats.candidate.application.command;

public record TransferApplicationCommand(Long applicationId, Long targetJobId, String note) {
}
