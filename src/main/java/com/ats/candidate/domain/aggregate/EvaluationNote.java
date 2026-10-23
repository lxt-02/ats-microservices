package com.ats.candidate.domain.aggregate;

import java.time.Instant;
import java.util.Objects;

/** UC-02.8: an internal comment on an application by a recruiter or hiring manager. */
public class EvaluationNote {

    public static final int MAX_LENGTH = 5000;

    private final Long id;
    private final Long applicationId;
    private final Long authorId;
    private final String content;
    private final Instant createdAt;

    private EvaluationNote(Long id, Long applicationId, Long authorId, String content, Instant createdAt) {
        this.id = id;
        this.applicationId = Objects.requireNonNull(applicationId, "Application is required");
        this.authorId = Objects.requireNonNull(authorId, "Author is required");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Note content is required");
        }
        if (content.trim().length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Note must not exceed 5,000 characters");
        }
        this.content = content.trim();
        this.createdAt = createdAt;
    }

    public static EvaluationNote write(Long applicationId, Long authorId, String content, Instant now) {
        return new EvaluationNote(null, applicationId, authorId, content, now);
    }

    public static EvaluationNote restore(Long id, Long applicationId, Long authorId, String content,
                                         Instant createdAt) {
        return new EvaluationNote(id, applicationId, authorId, content, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
