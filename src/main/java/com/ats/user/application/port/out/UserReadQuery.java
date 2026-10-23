package com.ats.user.application.port.out;

import com.ats.shared.web.PageResponse;
import com.ats.user.application.query.UserListQuery;
import com.ats.user.application.response.UserResponse;

public interface UserReadQuery {

    PageResponse<UserResponse> search(UserListQuery query);
}
