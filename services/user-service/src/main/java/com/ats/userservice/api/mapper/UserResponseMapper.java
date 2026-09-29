package com.ats.userservice.api.mapper;

import com.ats.userservice.api.dto.response.UserResponse;
import com.ats.userservice.domain.model.user.aggregate.User;
import org.springframework.stereotype.Component;

@Component
public class UserResponseMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId() == null ? null : user.getId().value(),
                user.getDepartmentId() == null ? null : user.getDepartmentId().value(),
                user.getFullName(),
                user.getEmail().value(),
                user.getPhoneNumber() == null ? null : user.getPhoneNumber().value(),
                user.getRole(),
                user.getAuthProvider(),
                user.getExternalSubjectId(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getCreatedBy(),
                user.getUpdatedBy()
        );
    }
}
