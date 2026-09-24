package com.ats.apigateway.config;

import java.net.InetSocketAddress;
import java.util.List;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String ANONYMOUS_KEY = "anonymous";

    @Bean
    public KeyResolver userRateLimitKeyResolver() {
        return exchange -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String userId = headers.getFirst(USER_ID_HEADER);
            if (StringUtils.hasText(userId)) {
                return Mono.just("user:" + userId.trim());
            }

            String forwardedFor = headers.getFirst(FORWARDED_FOR_HEADER);
            if (StringUtils.hasText(forwardedFor)) {
                return Mono.just("ip:" + forwardedFor.split(",")[0].trim());
            }

            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                return Mono.just("ip:" + remoteAddress.getAddress().getHostAddress());
            }

            List<String> hostHeaders = headers.get(HttpHeaders.HOST);
            if (hostHeaders != null && !hostHeaders.isEmpty()) {
                return Mono.just("host:" + hostHeaders.get(0));
            }

            return Mono.just(ANONYMOUS_KEY);
        };
    }
}
