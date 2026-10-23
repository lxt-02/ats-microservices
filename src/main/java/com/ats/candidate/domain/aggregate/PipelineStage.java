package com.ats.candidate.domain.aggregate;

import com.ats.candidate.domain.enums.ApplicationStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A Kanban column (UC-04.3). Each column is mapped to one pipeline state; the admin chooses its label, order and
 * colour. The states and moves themselves are fixed by BRULE-12.
 */
public class PipelineStage {

    private static final Pattern COLOR = Pattern.compile("^#[0-9a-fA-F]{6}$");

    private final Long id;
    private final ApplicationStatus status;
    private String name;
    private int order;
    private String color;
    private Instant updatedAt;
    private String updatedBy;

    private PipelineStage(Long id, ApplicationStatus status, String name, int order, String color, Instant updatedAt,
                          String updatedBy) {
        this.id = id;
        this.status = Objects.requireNonNull(status, "Stage status is required");
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        apply(name, order, color);
    }

    public static PipelineStage restore(Long id, ApplicationStatus status, String name, int order, String color,
                                        Instant updatedAt, String updatedBy) {
        return new PipelineStage(id, status, name, order, color, updatedAt, updatedBy);
    }

    public void configure(String newName, int newOrder, String newColor, String actor, Instant now) {
        apply(newName, newOrder, newColor);
        updatedAt = now;
        updatedBy = actor;
    }

    private void apply(String newName, int newOrder, String newColor) {
        if (newName == null || newName.isBlank() || newName.trim().length() > 100) {
            throw new IllegalArgumentException("Stage name is required (max 100 characters)");
        }
        if (newOrder < 1 || newOrder > 100) {
            throw new IllegalArgumentException("Stage order must be between 1 and 100");
        }
        if (newColor != null && !newColor.isBlank() && !COLOR.matcher(newColor.trim()).matches()) {
            throw new IllegalArgumentException("Colour must look like #4f46e5");
        }
        name = newName.trim();
        order = newOrder;
        color = newColor == null || newColor.isBlank() ? null : newColor.trim();
    }

    public Long getId() {
        return id;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public String getColor() {
        return color;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
