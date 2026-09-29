package com.ats.userservice.application.port.in;

import com.ats.userservice.application.command.sso.ChangeSsoStatusCommand;
import com.ats.userservice.application.command.sso.CreateSsoConfigurationCommand;
import com.ats.userservice.application.command.sso.UpdateSsoConfigurationCommand;
import com.ats.userservice.domain.model.sso.aggregate.SsoConfiguration;

public interface SsoConfigurationUseCase {

    SsoConfiguration create(CreateSsoConfigurationCommand command);

    SsoConfiguration getById(Long id);

    SsoConfiguration getActiveKeycloak();

    SsoConfiguration getActiveGoogle();

    SsoConfiguration update(UpdateSsoConfigurationCommand command);

    SsoConfiguration changeStatus(ChangeSsoStatusCommand command);

    void delete(Long id, String updatedBy);
}
