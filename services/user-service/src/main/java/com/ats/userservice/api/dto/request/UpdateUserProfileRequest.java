package com.ats.userservice.api.dto.request;

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
public class UpdateUserProfileRequest {

    private Long departmentId;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    @Size(max = 255, message = "Actor must not exceed 255 characters")
    private String actor;

}
