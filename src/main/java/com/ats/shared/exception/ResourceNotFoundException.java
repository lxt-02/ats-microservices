package com.ats.shared.exception;

/** HTTP 404. */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
