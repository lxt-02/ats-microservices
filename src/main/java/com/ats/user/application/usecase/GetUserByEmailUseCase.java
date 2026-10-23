package com.ats.user.application.usecase;

import com.ats.shared.exception.ResourceNotFoundException;
import com.ats.user.application.response.UserResponse;
import com.ats.user.domain.repository.UserRepository;
import com.ats.user.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUserByEmailUseCase {

    private final UserRepository userRepository;

    public GetUserByEmailUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse execute(String email) {
        Email normalizedEmail = Email.of(email);
        return userRepository.findByEmail(normalizedEmail)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by email: " + normalizedEmail.value()));
    }
}
