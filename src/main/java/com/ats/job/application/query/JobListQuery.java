package com.ats.job.application.query;

import com.ats.job.domain.enums.JobStatus;

/**
 * Job Management list. {@code keyword} matches the title or the numeric job id.
 * {@code sortKey}: createdAt | title | publishedAt | deadline.
 */
public record JobListQuery(String keyword, JobStatus status, String sortKey, boolean ascending, int page, int size) {
}
