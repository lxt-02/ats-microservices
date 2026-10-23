package com.ats.auth.application.usecase;

import com.ats.shared.exception.AuthenticationFailedException;

/** Credential input checks shared by the login use cases. */
final class Inputs {

    private Inputs() {
    }

    static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new AuthenticationFailedException(message);
        }
        return value.trim();
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
