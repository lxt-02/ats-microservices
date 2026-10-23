package com.ats.shared.exception;

/** HTTP 401: credentials (token, Google login...) are missing, invalid or expired. */
public class AuthenticationFailedException extends ApplicationException {

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
