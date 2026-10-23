package com.ats.user.application.usecase;

import com.ats.user.application.command.CreateLocalUserCommand;
import com.ats.user.application.response.UserResponse;
import com.ats.user.application.service.UserLoader;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.UserRole;
import com.ats.user.domain.repository.UserRepository;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateLocalUserUseCase {

    private final UserRepository userRepository;
    private final UserLoader userLoader;

    public CreateLocalUserUseCase(UserRepository userRepository, UserLoader userLoader) {
        this.userRepository = userRepository;
        this.userLoader = userLoader;
    }

    @Transactional
    public UserResponse execute(CreateLocalUserCommand command, String actor) {
        Email email = Email.of(command.email());
        userLoader.ensureEmailAvailable(email);
        userLoader.ensureDepartmentExists(command.departmentId());

        UserRole role = command.role() == null ? UserRole.CANDIDATE : command.role();
        User user = User.create(command.fullName(), email, command.passwordHash(), PhoneNumber.of(command.phone()),
                role, command.departmentId(), actor);
        return UserResponse.from(userRepository.save(user));
    }
}
