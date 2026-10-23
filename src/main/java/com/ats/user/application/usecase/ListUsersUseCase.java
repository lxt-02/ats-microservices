package com.ats.user.application.usecase;

import com.ats.shared.web.PageResponse;
import com.ats.user.application.port.out.UserReadQuery;
import com.ats.user.application.query.UserListQuery;
import com.ats.user.application.response.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsersUseCase {

    private final UserReadQuery userReadQuery;

    public ListUsersUseCase(UserReadQuery userReadQuery) {
        this.userReadQuery = userReadQuery;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> execute(UserListQuery query) {
        return userReadQuery.search(query);
    }
}
