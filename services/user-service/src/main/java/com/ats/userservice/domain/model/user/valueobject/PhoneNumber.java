package com.ats.userservice.domain.model.user.valueobject;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9 .-]{7,30}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            value = null;
        } else if (!PHONE_PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Phone number is invalid");
        } else {
            value = value.trim();
        }
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }
}
