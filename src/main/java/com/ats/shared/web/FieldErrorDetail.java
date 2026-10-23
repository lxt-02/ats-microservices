package com.ats.shared.web;

/** One validation error: the request field, the constraint code (NotBlank, Size...) and its message. */
public record FieldErrorDetail(String field, String code, String message) {
}
