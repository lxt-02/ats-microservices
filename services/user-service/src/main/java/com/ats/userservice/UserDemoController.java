package com.ats.userservice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserDemoController {

    private final String port;
    private final String instanceId;
    private final String hostname;

    public UserDemoController(
            @Value("${server.port}") String port,
            @Value("${eureka.instance.instance-id:${spring.application.name}:${random.value}}") String instanceId) {
        this.port = port;
        this.instanceId = instanceId;
        this.hostname = resolveHostname();
    }

    @GetMapping("/ping")
    public Map<String, Object> ping(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return Map.of(
                "service", "user-service",
                "instanceId", instanceId,
                "hostname", hostname,
                "port", port,
                "userId", userId == null ? "anonymous" : userId,
                "timestamp", Instant.now().toString()
        );
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "unknown";
        }
    }
}
