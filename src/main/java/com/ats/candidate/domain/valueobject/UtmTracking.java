package com.ats.candidate.domain.valueobject;

/** Sourcing analytics parameters. */
public record UtmTracking(String source, String medium, String campaign) {

    public UtmTracking {
        source = clean(source, 150);
        medium = clean(medium, 150);
        campaign = clean(campaign, 255);
    }

    public static UtmTracking none() {
        return new UtmTracking(null, null, null);
    }

    private static String clean(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}
