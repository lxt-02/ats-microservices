package com.ats.user.api.request;

import com.ats.shared.web.BasePagination;
import com.ats.user.application.query.UserListQuery;
import com.ats.user.domain.enums.UserRole;
import com.ats.user.domain.enums.UserStatus;
import jakarta.validation.constraints.Size;

/** Query string of GET /api/v1/users, e.g. {@code ?role=HIRING_MANAGER&status=ACTIVE&keyword=nguyen}. */
public record ListUserRequest(
        @Size(max = 255) String keyword,
        UserRole role,
        UserStatus status,
        Integer page,
        Integer size,
        String sort
) implements BasePagination {

    public UserListQuery toQuery() {
        return new UserListQuery(keyword, role, status, pageOrDefault(), sizeOrDefault());
    }
}
