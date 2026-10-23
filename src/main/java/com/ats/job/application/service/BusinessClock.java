package com.ats.job.application.service;

import com.ats.shared.infrastructure.time.ClockConfiguration;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** "Now" and "today" in the business time zone (deadlines are calendar dates in Vietnam). */
@Component
public class BusinessClock {

    private final Clock clock;

    public BusinessClock(Clock clock) {
        this.clock = clock;
    }

    public Instant now() {
        return clock.instant();
    }

    public LocalDateTime localNow() {
        return LocalDateTime.now(clock.withZone(ClockConfiguration.BUSINESS_ZONE));
    }

    public LocalDate today() {
        return LocalDate.now(clock.withZone(ClockConfiguration.BUSINESS_ZONE));
    }
}
