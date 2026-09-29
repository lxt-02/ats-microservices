package com.ats.userservice.application.port.in;

import com.ats.userservice.application.command.user.ChangeUserRoleCommand;
import com.ats.userservice.application.command.user.ChangeUserStatusCommand;
import com.ats.userservice.application.command.user.CreateExternalUserCommand;
import com.ats.userservice.application.command.user.CreateLocalUserCommand;
import com.ats.userservice.application.command.user.UpdateUserProfileCommand;
import com.ats.userservice.domain.model.user.aggregate.User;

public interface UserUseCase {

    User createLocal(CreateLocalUserCommand command);

    User createExternal(CreateExternalUserCommand command);

    User getById(Long id);

    User getByEmail(String email);

    User updateProfile(UpdateUserProfileCommand command);

    User changeRole(ChangeUserRoleCommand command);

    User changeStatus(ChangeUserStatusCommand command);

    void delete(Long id, String updatedBy);
}
