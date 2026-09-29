package com.ats.userservice.application.service;

import com.ats.userservice.application.command.user.ChangeUserRoleCommand;
import com.ats.userservice.application.command.user.ChangeUserStatusCommand;
import com.ats.userservice.application.command.user.CreateExternalUserCommand;
import com.ats.userservice.application.command.user.CreateLocalUserCommand;
import com.ats.userservice.application.command.user.UpdateUserProfileCommand;
import com.ats.userservice.application.exception.DuplicateResourceException;
import com.ats.userservice.application.exception.ResourceNotFoundException;
import com.ats.userservice.application.port.in.UserUseCase;
import com.ats.userservice.domain.model.department.valueobject.DepartmentId;
import com.ats.userservice.domain.model.user.aggregate.User;
import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.enums.UserRole;
import com.ats.userservice.domain.model.user.enums.UserStatus;
import com.ats.userservice.domain.model.user.valueobject.Email;
import com.ats.userservice.domain.model.user.valueobject.PhoneNumber;
import com.ats.userservice.domain.model.user.valueobject.UserId;
import com.ats.userservice.domain.repository.DepartmentRepository;
import com.ats.userservice.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserManagementService implements UserUseCase {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public UserManagementService(UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public User createLocal(CreateLocalUserCommand command) {
        Email email = Email.of(command.email());
        ensureEmailAvailable(email);
        DepartmentId departmentId = toDepartmentId(command.departmentId());
        ensureDepartmentExists(departmentId);

        UserRole role = command.role() == null ? UserRole.CANDIDATE : command.role();
        User user = User.create(command.fullName(), email, command.passwordHash(), PhoneNumber.of(command.phone()),
                role, departmentId, command.createdBy());
        return userRepository.save(user);
    }

    @Override
    public User createExternal(CreateExternalUserCommand command) {
        Email email = Email.of(command.email());
        ensureEmailAvailable(email);
        DepartmentId departmentId = toDepartmentId(command.departmentId());
        ensureDepartmentExists(departmentId);

        AuthProvider authProvider = command.authProvider() == null ? AuthProvider.KEYCLOAK : command.authProvider();
        userRepository.findByAuthProviderAndExternalSubjectId(authProvider, command.externalSubjectId())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("External user already exists");
                });

        UserRole role = command.role() == null ? UserRole.CANDIDATE : command.role();
        User user = User.createExternal(command.fullName(), email, PhoneNumber.of(command.phone()), role,
                departmentId, authProvider, command.externalSubjectId(), command.createdBy());
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return findUser(UserId.of(id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        Email normalizedEmail = Email.of(email);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by email: " + normalizedEmail.value()));
    }

    @Override
    public User updateProfile(UpdateUserProfileCommand command) {
        User user = findUser(UserId.of(command.id()));
        DepartmentId departmentId = toDepartmentId(command.departmentId());
        ensureDepartmentExists(departmentId);

        user.changeProfile(command.fullName(), PhoneNumber.of(command.phone()), departmentId, command.updatedBy());
        return userRepository.save(user);
    }

    @Override
    public User changeRole(ChangeUserRoleCommand command) {
        User user = findUser(UserId.of(command.id()));
        user.changeRole(command.role(), command.updatedBy());
        return userRepository.save(user);
    }

    @Override
    public User changeStatus(ChangeUserStatusCommand command) {
        User user = findUser(UserId.of(command.id()));
        if (command.status() == UserStatus.ACTIVE) {
            user.activate(command.updatedBy());
        } else if (command.status() == UserStatus.INACTIVE) {
            user.deactivate(command.updatedBy());
        } else if (command.status() == UserStatus.LOCKED) {
            user.lock(command.updatedBy());
        } else {
            throw new IllegalArgumentException("User status is required");
        }
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id, String updatedBy) {
        User user = findUser(UserId.of(id));
        user.markDeleted(updatedBy);
        userRepository.save(user);
    }

    private User findUser(UserId id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id.value()));
    }

    private void ensureEmailAvailable(Email email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists: " + email.value());
        }
    }

    private void ensureDepartmentExists(DepartmentId id) {
        if (id == null) {
            return;
        }
        departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id.value()));
    }

    private DepartmentId toDepartmentId(Long value) {
        return value == null ? null : DepartmentId.of(value);
    }
}
