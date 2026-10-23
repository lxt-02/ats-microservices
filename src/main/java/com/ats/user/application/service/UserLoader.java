package com.ats.user.application.service;

import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.shared.exception.ConflictException;
import com.ats.shared.exception.ResourceNotFoundException;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.repository.UserRepository;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.UserId;
import org.springframework.stereotype.Service;

/** Lookups and checks shared by the user use cases. */
@Service
public class UserLoader {

    private final UserRepository userRepository;
    private final DepartmentDirectory departmentDirectory;

    public UserLoader(UserRepository userRepository, DepartmentDirectory departmentDirectory) {
        this.userRepository = userRepository;
        this.departmentDirectory = departmentDirectory;
    }

    public User load(Long id) {
        return userRepository.findById(UserId.of(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public void ensureEmailAvailable(Email email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists: " + email.value());
        }
    }

    /** Checks an optional department reference through the Organization module. */
    public void ensureDepartmentExists(Long departmentId) {
        if (departmentId != null && !departmentDirectory.exists(departmentId)) {
            throw new ResourceNotFoundException("Department not found: " + departmentId);
        }
    }
}
