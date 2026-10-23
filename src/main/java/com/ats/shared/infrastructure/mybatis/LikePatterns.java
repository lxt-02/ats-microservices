package com.ats.shared.infrastructure.mybatis;

/** Escapes user input for {@code LIKE ... ESCAPE '\'}. */
public final class LikePatterns {

    private LikePatterns() {
    }

    public static String escape(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** {@code %value%} in lower case, or null for blank input. */
    public static String containsLower(String value) {
        return value == null || value.isBlank() ? null : "%" + escape(value.trim().toLowerCase()) + "%";
    }
}
