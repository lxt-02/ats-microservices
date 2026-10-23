package com.ats.auth.infrastructure.security;

import com.ats.auth.infrastructure.configuration.AuthSecurityProperties;
import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.web.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Authenticates every /api/** request except the public paths (login, refresh, logout).
 * A valid {@code Authorization: Bearer <ATS access token>} becomes a {@link CurrentUser} request attribute;
 * anything else gets 401 before reaching a controller. Identity is never read from client headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final AuthSecurityProperties properties;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, AuthSecurityProperties properties,
                                   ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith(API_PREFIX) || properties.getPublicPaths().stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            unauthorized(response, "Authentication is required");
            return;
        }
        try {
            CurrentUser currentUser = jwtTokenService.verifyAccessToken(
                    authorization.substring(BEARER_PREFIX.length()).trim());
            request.setAttribute(CurrentUser.REQUEST_ATTRIBUTE, currentUser);
        } catch (AuthenticationFailedException ex) {
            unauthorized(response, "Access token is invalid or expired");
            return;
        }
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), message));
    }
}
