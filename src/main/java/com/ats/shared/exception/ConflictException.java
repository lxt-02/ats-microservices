package com.ats.shared.exception;

/** HTTP 409: the request clashes with existing data (duplicate email, identity already linked...). */
public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(message);
    }
}
