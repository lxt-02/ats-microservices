package com.ats.user.application.usecase;

import com.ats.user.application.service.UserLoader;
import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Soft delete. */
@Service
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final UserLoader userLoader;

    public DeleteUserUseCase(UserRepository userRepository, UserLoader userLoader) {
        this.userRepository = userRepository;
        this.userLoader = userLoader;
    }

    @Transactional
    public void execute(Long id, String actor) {
        User user = userLoader.load(id);
        user.markDeleted(actor);
        userRepository.save(user);
    }
}
