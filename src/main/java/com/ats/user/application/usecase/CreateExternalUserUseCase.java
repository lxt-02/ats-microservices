package com.ats.user.application.usecase;

import com.ats.shared.exception.ConflictException;
import com.ats.user.application.command.CreateExternalUserCommand;
import com.ats.user.application.response.UserResponse;
import com.ats.user.application.service.UserLoader;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.AuthProvider;
import com.ats.user.domain.enums.UserRole;
import com.ats.user.domain.repository.UserRepository;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateExternalUserUseCase {

    private final UserRepository userRepository;
    private final UserLoader userLoader;

    public CreateExternalUserUseCase(UserRepository userRepository, UserLoader userLoader) {
        this.userRepository = userRepository;
        this.userLoader = userLoader;
    }

    @Transactional
    public UserResponse execute(CreateExternalUserCommand command, String actor) {
        Email email = Email.of(command.email());
        userLoader.ensureEmailAvailable(email);
        userLoader.ensureDepartmentExists(command.departmentId());

        AuthProvider authProvider = command.authProvider() == null ? AuthProvider.KEYCLOAK : command.authProvider();
        userRepository.findByAuthProviderAndExternalSubjectId(authProvider, command.externalSubjectId())
                .ifPresent(existing -> {
                    throw new ConflictException("External user already exists");
                });

        UserRole role = command.role() == null ? UserRole.CANDIDATE : command.role();
        User user = User.createExternal(command.fullName(), email, PhoneNumber.of(command.phone()), role,
                command.departmentId(), authProvider, command.externalSubjectId(), actor);
        return UserResponse.from(userRepository.save(user));
    }
}
