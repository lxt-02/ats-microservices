package com.ats.auth.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Paths under /api/ that do not need an access token (prefix match). */
@Component
@ConfigurationProperties(prefix = "ats.security")
public class AuthSecurityProperties {

    private List<String> publicPaths = new ArrayList<>(List.of(
            "/api/v1/auth/google/",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/public/"
    ));

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths == null ? new ArrayList<>() : new ArrayList<>(publicPaths);
    }
}
