package com.ats.auth.api.controller;

import com.ats.auth.api.http.GoogleOAuthStateCookie;
import com.ats.auth.api.http.GoogleOAuthStateCookie.StoredState;
import com.ats.auth.api.request.GoogleAuthorizationCodeLoginRequest;
import com.ats.auth.api.request.GoogleIdTokenLoginRequest;
import com.ats.auth.application.command.GoogleCallbackCommand;
import com.ats.auth.application.response.GoogleAuthorizationResponse;
import com.ats.auth.application.response.GoogleLoginResponse;
import com.ats.auth.application.usecase.CompleteGoogleAuthorizationUseCase;
import com.ats.auth.application.usecase.LoginWithGoogleCodeUseCase;
import com.ats.auth.application.usecase.LoginWithGoogleIdTokenUseCase;
import com.ats.auth.application.usecase.StartGoogleAuthorizationUseCase;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.web.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Google sign-in endpoints (public):
 * <ul>
 *   <li>GET  /authorize -> 302 to Google, sets the state cookie</li>
 *   <li>GET  /callback  -> Google redirects here; verifies state, returns ATS tokens</li>
 *   <li>POST /code      -> SPA flow: frontend handled the redirect and state itself</li>
 *   <li>POST /id-token  -> Google Identity Services (One Tap / button) flow</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth/google")
public class GoogleAuthController {

    private final StartGoogleAuthorizationUseCase startGoogleAuthorizationUseCase;
    private final CompleteGoogleAuthorizationUseCase completeGoogleAuthorizationUseCase;
    private final LoginWithGoogleCodeUseCase loginWithGoogleCodeUseCase;
    private final LoginWithGoogleIdTokenUseCase loginWithGoogleIdTokenUseCase;
    private final GoogleOAuthStateCookie stateCookie;

    public GoogleAuthController(StartGoogleAuthorizationUseCase startGoogleAuthorizationUseCase,
                                CompleteGoogleAuthorizationUseCase completeGoogleAuthorizationUseCase,
                                LoginWithGoogleCodeUseCase loginWithGoogleCodeUseCase,
                                LoginWithGoogleIdTokenUseCase loginWithGoogleIdTokenUseCase,
                                GoogleOAuthStateCookie stateCookie) {
        this.startGoogleAuthorizationUseCase = startGoogleAuthorizationUseCase;
        this.completeGoogleAuthorizationUseCase = completeGoogleAuthorizationUseCase;
        this.loginWithGoogleCodeUseCase = loginWithGoogleCodeUseCase;
        this.loginWithGoogleIdTokenUseCase = loginWithGoogleIdTokenUseCase;
        this.stateCookie = stateCookie;
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestParam(required = false) String redirectUri) {
        GoogleAuthorizationResponse authorization = startGoogleAuthorizationUseCase.execute(redirectUri);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(authorization.authorizationUri())
                .header(HttpHeaders.SET_COOKIE, stateCookie.create(authorization).toString())
                .cacheControl(CacheControl.noStore())
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<GoogleLoginResponse>> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @CookieValue(name = GoogleOAuthStateCookie.NAME, required = false) String stateCookieValue,
            HttpServletResponse response) {
        // The login attempt is single-use: drop the cookie whatever the outcome.
        response.addHeader(HttpHeaders.SET_COOKIE, stateCookie.clear().toString());

        if (error != null) {
            throw new AuthenticationFailedException("Google sign-in was cancelled or denied");
        }
        StoredState stored = stateCookie.parse(stateCookieValue)
                .orElseThrow(() -> new AuthenticationFailedException("Login session has expired, please try again"));

        return noStore(completeGoogleAuthorizationUseCase.execute(new GoogleCallbackCommand(
                code, state, stored.state(), stored.codeVerifier(), stored.redirectUri())));
    }

    @PostMapping("/code")
    public ResponseEntity<ApiResponse<GoogleLoginResponse>> loginWithAuthorizationCode(
            @Valid @RequestBody GoogleAuthorizationCodeLoginRequest request) {
        return noStore(loginWithGoogleCodeUseCase.execute(request.toCommand()));
    }

    @PostMapping("/id-token")
    public ResponseEntity<ApiResponse<GoogleLoginResponse>> loginWithIdToken(
            @Valid @RequestBody GoogleIdTokenLoginRequest request) {
        return noStore(loginWithGoogleIdTokenUseCase.execute(request.idToken()));
    }

    private static <T> ResponseEntity<ApiResponse<T>> noStore(T body) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(HttpStatus.OK.value(), body));
    }
}
