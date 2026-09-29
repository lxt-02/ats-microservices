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
public class GoogleAuthorizationCodeLoginRequest {

    @NotBlank(message = "Authorization code is required")
    private String code;

    @Size(max = 500, message = "Redirect URI must not exceed 500 characters")
    private String redirectUri;
}
