package com.ats.shared.infrastructure.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration(proxyBeanMethods = false)
public class ClockConfiguration {

    /** Business time zone (Vietnam). Timestamps are still stored as UTC instants. */
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
