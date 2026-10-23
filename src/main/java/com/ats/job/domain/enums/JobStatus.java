package com.ats.job.domain.enums;

/**
 * DRAFT (also "approved / ready to publish" once the Hiring Manager approved) -> PENDING_APPROVAL -> DRAFT
 * -> PUBLISHED -> CLOSED. See BRULE-11.
 */
public enum JobStatus {
    DRAFT,
    PENDING_APPROVAL,
    PUBLISHED,
    CLOSED
}
