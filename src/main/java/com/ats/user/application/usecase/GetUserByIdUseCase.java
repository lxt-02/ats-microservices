package com.ats.user.application.usecase;

import com.ats.user.application.response.UserResponse;
import com.ats.user.application.service.UserLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUserByIdUseCase {

    private final UserLoader userLoader;

    public GetUserByIdUseCase(UserLoader userLoader) {
        this.userLoader = userLoader;
    }

    @Transactional(readOnly = true)
    public UserResponse execute(Long id) {
        return UserResponse.from(userLoader.load(id));
    }
}
