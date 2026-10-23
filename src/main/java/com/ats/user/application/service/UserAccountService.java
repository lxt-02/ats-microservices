package com.ats.user.application.service;

import com.ats.shared.exception.ConflictException;
import com.ats.user.application.contract.UserAccountContract;
import com.ats.user.application.response.UserResponse;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.AuthProvider;
import com.ats.user.domain.enums.UserRole;
import com.ats.user.domain.repository.UserRepository;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.PhoneNumber;
import com.ats.user.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserAccountService implements UserAccountContract {

    private final UserRepository userRepository;
    private final UserLoader userLoader;

    public UserAccountService(UserRepository userRepository, UserLoader userLoader) {
        this.userRepository = userRepository;
        this.userLoader = userLoader;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(Long userId) {
        return userRepository.findById(UserId.of(userId)).map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(Email.of(email)).map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByExternalIdentity(String provider, String subject) {
        return userRepository.findByAuthProviderAndExternalSubjectId(AuthProvider.valueOf(provider), subject)
                .map(UserResponse::from);
    }

    @Override
    @Transactional
    public UserResponse registerExternalCandidate(String fullName, String email, String provider, String subject,
                                                  String actor) {
        Email normalizedEmail = Email.of(email);
        userLoader.ensureEmailAvailable(normalizedEmail);
        User candidate = User.createExternal(fullName, normalizedEmail, PhoneNumber.of(null), UserRole.CANDIDATE,
                null, AuthProvider.valueOf(provider), subject, actor);
        return UserResponse.from(userRepository.save(candidate));
    }

    @Override
    @Transactional
    public UserResponse linkExternalIdentity(Long userId, String provider, String subject, String actor) {
        User user = userLoader.load(userId);
        AuthProvider authProvider = AuthProvider.valueOf(provider);
        if (!user.canLinkExternalIdentity(authProvider)) {
            throw new ConflictException("This email is already registered with another sign-in method");
        }
        user.linkExternalIdentity(authProvider, subject, actor);
        return UserResponse.from(userRepository.save(user));
    }
}
