package com.ats.userservice.api.rest;

import com.ats.userservice.api.dto.request.GoogleAuthorizationCodeLoginRequest;
import com.ats.userservice.api.dto.request.GoogleIdTokenLoginRequest;
import com.ats.userservice.api.dto.response.GoogleLoginResponse;
import com.ats.userservice.api.mapper.UserResponseMapper;
import com.ats.userservice.application.service.GoogleAuthenticationService;
import com.ats.userservice.application.service.GoogleAuthenticationService.GoogleAuthenticationResult;
import com.ats.userservice.application.service.GoogleAuthenticationService.GoogleProfile;
import com.ats.userservice.domain.model.user.enums.AuthProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth/google")
public class GoogleAuthController {

    private final GoogleAuthenticationService googleAuthenticationService;
    private final UserResponseMapper userResponseMapper;

    public GoogleAuthController(GoogleAuthenticationService googleAuthenticationService,
                                UserResponseMapper userResponseMapper) {
        this.googleAuthenticationService = googleAuthenticationService;
        this.userResponseMapper = userResponseMapper;
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestParam(required = false) String state,
                                          @RequestParam(required = false) String redirectUri) {
        URI authorizationUri = googleAuthenticationService.buildAuthorizationUri(state, redirectUri);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, authorizationUri.toString())
                .build();
    }

    @GetMapping("/callback")
    public GoogleLoginResponse callback(@RequestParam String code) {
        return toResponse(googleAuthenticationService.loginWithAuthorizationCode(code, null));
    }

    @PostMapping("/code")
    public GoogleLoginResponse loginWithAuthorizationCode(
            @Valid @RequestBody GoogleAuthorizationCodeLoginRequest request) {
        return toResponse(googleAuthenticationService.loginWithAuthorizationCode(
                request.getCode(),
                request.getRedirectUri()
        ));
    }

    @PostMapping("/id-token")
    public GoogleLoginResponse loginWithIdToken(@Valid @RequestBody GoogleIdTokenLoginRequest request) {
        return toResponse(googleAuthenticationService.loginWithIdToken(request.getIdToken()));
    }

    private GoogleLoginResponse toResponse(GoogleAuthenticationResult result) {
        GoogleProfile profile = result.profile();
        return new GoogleLoginResponse(
                AuthProvider.GOOGLE,
                profile.subject(),
                profile.email(),
                profile.emailVerified(),
                profile.name(),
                profile.pictureUrl(),
                profile.expiresAtEpochSeconds(),
                userResponseMapper.toResponse(result.user())
        );
    }
}
