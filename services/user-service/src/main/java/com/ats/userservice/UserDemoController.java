package com.ats.userservice;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserDemoController {

    @GetMapping("/ping")
    public Map<String, Object> ping(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return Map.of(
                "service", "user-service",
                "userId", userId == null ? "anonymous" : userId,
                "timestamp", Instant.now().toString()
        );
    }
}
