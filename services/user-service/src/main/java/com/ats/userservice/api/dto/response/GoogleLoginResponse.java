package com.ats.userservice.api.dto.response;

import com.ats.userservice.domain.model.user.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginResponse {

    private AuthProvider provider;
    private String subject;
    private String email;
    private boolean emailVerified;
    private String name;
    private String pictureUrl;
    private Long expiresAtEpochSeconds;
    private UserResponse user;
}
