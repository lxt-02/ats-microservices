package com.ats.auth.api.request;

import com.ats.auth.application.command.ChangeSsoConfigurationStatusCommand;

public record ChangeSsoConfigurationStatusRequest(boolean active) {

    public ChangeSsoConfigurationStatusCommand toCommand(Long id) {
        return new ChangeSsoConfigurationStatusCommand(id, active);
    }
}
