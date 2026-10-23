package com.ats.shared.exception;

/** HTTP 409: an application-level business rule refuses the operation. */
public class BusinessRuleException extends ApplicationException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
