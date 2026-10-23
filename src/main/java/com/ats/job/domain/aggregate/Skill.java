package com.ats.job.domain.aggregate;

import java.time.Instant;

/** Skill master data (e.g. Java, Project Management), grouped by category. */
public class Skill {

    private final Long id;
    private final String name;
    private final String category;
    private final Instant createdAt;
    private final String createdBy;

    private Skill(Long id, String name, String category, Instant createdAt, String createdBy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        this.id = id;
        this.name = name.trim();
        this.category = category == null || category.isBlank() ? null : category.trim();
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public static Skill create(String name, String category, String actor, Instant now) {
        return new Skill(null, name, category, now, actor);
    }

    public static Skill restore(Long id, String name, String category, Instant createdAt, String createdBy) {
        return new Skill(id, name, category, createdAt, createdBy);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
