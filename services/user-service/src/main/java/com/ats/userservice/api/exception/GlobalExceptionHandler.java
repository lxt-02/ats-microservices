package com.ats.userservice.api.exception;

import com.ats.userservice.application.exception.DuplicateResourceException;
import com.ats.userservice.application.exception.AuthenticationFailedException;
import com.ats.userservice.application.exception.ResourceNotFoundException;
import com.ats.userservice.domain.model.department.exception.DepartmentDomainException;
import com.ats.userservice.domain.model.sso.exception.SsoConfigurationDomainException;
import com.ats.userservice.domain.model.user.exception.UserDomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            UserDomainException.class,
            DepartmentDomainException.class,
            SsoConfigurationDomainException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(DuplicateResourceException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationFailed(AuthenticationFailedException ex,
                                                                      HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", Map.of(), request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, Map<String, String> errors,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI(),
                        errors
                ));
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> errors
    ) {
    }
}
