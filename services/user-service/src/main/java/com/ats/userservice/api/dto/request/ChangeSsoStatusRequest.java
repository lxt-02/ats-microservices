package com.ats.userservice.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSsoStatusRequest {

    private boolean active;

    @Size(max = 255, message = "Actor must not exceed 255 characters")
    private String actor;

}
