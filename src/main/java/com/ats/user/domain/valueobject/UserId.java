package com.ats.user.domain.valueobject;

public record UserId(Long value) {

    public UserId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }

    public static UserId of(Long value) {
        return new UserId(value);
    }
}
