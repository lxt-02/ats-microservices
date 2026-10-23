package com.ats.auth.application.command;

public record ChangeSsoConfigurationStatusCommand(Long id, boolean active) {
}
