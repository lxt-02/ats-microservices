package com.ats.candidate.application.command;

import com.ats.candidate.domain.enums.ApplicationStatus;

public record MoveApplicationCommand(Long applicationId, ApplicationStatus toStatus, String note) {
}
