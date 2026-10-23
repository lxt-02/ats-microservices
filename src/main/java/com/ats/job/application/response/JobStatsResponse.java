package com.ats.job.application.response;

/** KPI cards of the Job Management screen. */
public record JobStatsResponse(long total, long published, long draft, long pendingApproval, long closed) {
}
