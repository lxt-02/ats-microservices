package com.ats.user.application.usecase;

import com.ats.user.application.command.UpdateUserProfileCommand;
import com.ats.user.application.response.UserResponse;
import com.ats.user.application.service.UserLoader;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.repository.UserRepository;
import com.ats.user.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final UserLoader userLoader;

    public UpdateUserProfileUseCase(UserRepository userRepository, UserLoader userLoader) {
        this.userRepository = userRepository;
        this.userLoader = userLoader;
    }

    @Transactional
    public UserResponse execute(UpdateUserProfileCommand command, String actor) {
        User user = userLoader.load(command.id());
        userLoader.ensureDepartmentExists(command.departmentId());

        user.changeProfile(command.fullName(), PhoneNumber.of(command.phone()), command.departmentId(), actor);
        return UserResponse.from(userRepository.save(user));
    }
}
