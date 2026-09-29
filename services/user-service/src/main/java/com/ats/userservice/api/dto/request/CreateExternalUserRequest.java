package com.ats.userservice.api.dto.request;

import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateExternalUserRequest {

    private Long departmentId;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    private UserRole role;

    private AuthProvider authProvider;

    @NotBlank(message = "External subject id is required")
    @Size(max = 255, message = "External subject id must not exceed 255 characters")
    private String externalSubjectId;

    @Size(max = 255, message = "Actor must not exceed 255 characters")
    private String actor;

}
