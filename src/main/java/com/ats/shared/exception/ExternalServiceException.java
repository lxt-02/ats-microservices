package com.ats.shared.exception;

/** HTTP 503: an upstream dependency (e.g. Google) is unreachable or failing. */
public class ExternalServiceException extends ApplicationException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
