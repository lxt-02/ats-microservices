package com.ats.userservice.api.dto.request;

import com.ats.userservice.domain.model.user.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserRoleRequest {

    @NotNull(message = "User role is required")
    private UserRole role;

    @Size(max = 255, message = "Actor must not exceed 255 characters")
    private String actor;

}
