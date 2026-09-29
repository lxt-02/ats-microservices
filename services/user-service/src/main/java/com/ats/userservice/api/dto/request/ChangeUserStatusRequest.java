package com.ats.userservice.api.dto.request;

import com.ats.userservice.domain.model.user.enums.UserStatus;
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
public class ChangeUserStatusRequest {

    @NotNull(message = "User status is required")
    private UserStatus status;

    @Size(max = 255, message = "Actor must not exceed 255 characters")
    private String actor;

}
