package com.ats.user.application.usecase;

import com.ats.user.application.command.ChangeUserRoleCommand;
import com.ats.user.application.response.UserResponse;
import com.ats.user.application.service.UserLoader;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeUserRoleUseCase {

    private final UserRepository userRepository;
    private final UserLoader userLoader;

    public ChangeUserRoleUseCase(UserRepository userRepository, UserLoader userLoader) {
        this.userRepository = userRepository;
        this.userLoader = userLoader;
    }

    @Transactional
    public UserResponse execute(ChangeUserRoleCommand command, String actor) {
        User user = userLoader.load(command.id());
        user.changeRole(command.role(), actor);
        return UserResponse.from(userRepository.save(user));
    }
}
