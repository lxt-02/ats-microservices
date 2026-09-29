package com.ats.userservice.api.dto.response;

import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.enums.UserRole;
import com.ats.userservice.domain.model.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private Long departmentId;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private AuthProvider authProvider;
    private String externalSubjectId;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
