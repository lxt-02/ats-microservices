package com.ats.user.application.usecase;

import com.ats.user.application.command.ChangeUserStatusCommand;
import com.ats.user.application.response.UserResponse;
import com.ats.user.application.service.UserLoader;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeUserStatusUseCase {

    private final UserRepository userRepository;
    private final UserLoader userLoader;

    public ChangeUserStatusUseCase(UserRepository userRepository, UserLoader userLoader) {
        this.userRepository = userRepository;
        this.userLoader = userLoader;
    }

    @Transactional
    public UserResponse execute(ChangeUserStatusCommand command, String actor) {
        if (command.status() == null) {
            throw new IllegalArgumentException("User status is required");
        }
        User user = userLoader.load(command.id());
        switch (command.status()) {
            case ACTIVE -> user.activate(actor);
            case INACTIVE -> user.deactivate(actor);
            case LOCKED -> user.lock(actor);
        }
        return UserResponse.from(userRepository.save(user));
    }
}
