package com.ats.shared.web;

import com.ats.shared.exception.AuthenticationFailedException;
import com.ats.shared.exception.BusinessRuleException;
import com.ats.shared.exception.ConflictException;
import com.ats.shared.exception.ExternalServiceException;
import com.ats.shared.exception.ResourceNotFoundException;
import com.ats.shared.exception.RuleViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps exceptions to HTTP status + {@link ApiResponse}:
 * validation 400 (data = field errors), IllegalArgument 400, RuleViolation 400/409 (data = {rule, ...}),
 * authentication 401, AccessDenied 403, not found 404, conflict / business rule 409, upstream 503, others 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<List<FieldErrorDetail>>> handleValidation(BindException ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors(ex.getBindingResult()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", null);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(RuleViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleRuleViolation(RuleViolationException ex) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rule", ex.getRule());
        data.putAll(ex.getDetails());
        HttpStatus status = ex.isInvalidInput() ? HttpStatus.BAD_REQUEST : HttpStatus.CONFLICT;
        return build(status, ex.getMessage(), data);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler({ConflictException.class, BusinessRuleException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalService(ExternalServiceException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        // Spring MVC's own errors (404 no route, 405, 415, missing parameter...) carry their status.
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            String detail = errorResponse.getBody().getDetail();
            return ResponseEntity.status(status)
                    .body(ApiResponse.error(status.value(), detail == null ? ex.getMessage() : detail));
        }
        log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", null);
    }

    private static List<FieldErrorDetail> fieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getCode(), error.getDefaultMessage()))
                .toList();
    }

    private static <T> ResponseEntity<ApiResponse<T>> build(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(ApiResponse.error(status.value(), message, data));
    }
}
