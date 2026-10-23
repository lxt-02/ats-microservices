package com.ats.auth.api.controller;

import com.ats.auth.api.request.RefreshTokenRequest;
import com.ats.auth.application.response.AuthTokenResponse;
import com.ats.auth.application.usecase.LogoutUseCase;
import com.ats.auth.application.usecase.RefreshSessionUseCase;
import com.ats.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthTokenController {

    private final RefreshSessionUseCase refreshSessionUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthTokenController(RefreshSessionUseCase refreshSessionUseCase, LogoutUseCase logoutUseCase) {
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    /** Rotates the refresh token: the old one stops working, use the new one from the response. */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(HttpStatus.OK.value(), refreshSessionUseCase.execute(request.refreshToken())));
    }

    /** Revokes the given refresh token. Idempotent. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.execute(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
