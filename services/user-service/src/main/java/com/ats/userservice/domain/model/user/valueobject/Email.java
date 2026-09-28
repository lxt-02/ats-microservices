package com.ats.userservice.domain.model.user.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        value = value.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email is invalid");
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }
}
