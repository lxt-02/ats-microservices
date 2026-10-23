package com.ats.candidate.application.service;

import com.ats.shared.infrastructure.time.ClockConfiguration;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Component
public class CandidateClock {

    private final Clock clock;

    public CandidateClock(Clock clock) {
        this.clock = clock;
    }

    public Instant now() {
        return clock.instant();
    }

    public ZoneId zone() {
        return ClockConfiguration.BUSINESS_ZONE;
    }
}
