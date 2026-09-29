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
public class DepartmentRequest {

    private Long parentId;

    @NotBlank(message = "Department name is required")
    @Size(max = 255, message = "Department name must not exceed 255 characters")
    private String name;

    private String description;

    @Size(max = 255, message = "Actor must not exceed 255 characters")
    private String actor;

}
