package com.ats.candidate.domain.aggregate;

import com.ats.candidate.domain.enums.ApplicationSource;
import com.ats.candidate.domain.enums.ApplicationStatus;
import com.ats.candidate.domain.valueobject.UtmTracking;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

/** A candidate's application to a job (job referenced by id). Starts in status NEW. */
public class JobApplication {

    private final Long id;
    private final Long jobId;
    private final Long candidateId;
    private final Long cvId;
    private ApplicationStatus status;
    private final String coverLetter;
    private final ApplicationSource source;
    private final String referralCode;
    private final UtmTracking utm;
    private final Instant appliedAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private JobApplication(Long id, Long jobId, Long candidateId, Long cvId, ApplicationStatus status,
                           String coverLetter, ApplicationSource source, String referralCode, UtmTracking utm,
                           Instant appliedAt, String createdBy, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.jobId = Objects.requireNonNull(jobId, "Job is required");
        this.candidateId = Objects.requireNonNull(candidateId, "Candidate is required");
        this.cvId = cvId;
        this.status = Objects.requireNonNull(status, "Application status is required");
        this.coverLetter = coverLetter == null || coverLetter.isBlank() ? null : coverLetter.trim();
        this.source = source;
        this.referralCode = referralCode == null || referralCode.isBlank() ? null : referralCode.trim();
        this.utm = utm == null ? UtmTracking.none() : utm;
        this.appliedAt = appliedAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static JobApplication submit(Long jobId, Long candidateId, Long cvId, String coverLetter,
                                        ApplicationSource source, String referralCode, UtmTracking utm,
                                        String actor, Instant now) {
        return new JobApplication(null, jobId, candidateId, cvId, ApplicationStatus.NEW, coverLetter, source,
                referralCode, utm, now, actor, now, actor);
    }

    public static JobApplication restore(Long id, Long jobId, Long candidateId, Long cvId, ApplicationStatus status,
                                         String coverLetter, ApplicationSource source, String referralCode,
                                         UtmTracking utm, Instant appliedAt, String createdBy, Instant updatedAt,
                                         String updatedBy) {
        return new JobApplication(id, jobId, candidateId, cvId, status, coverLetter, source, referralCode, utm,
                appliedAt, createdBy, updatedAt, updatedBy);
    }

    /** Application ID shown to candidates: APP-YYYY-NNNNN. */
    public static String code(Long id, Instant appliedAt, ZoneId zone) {
        return String.format("APP-%d-%05d", appliedAt.atZone(zone).getYear(), id);
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public Long getCvId() {
        return cvId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public ApplicationSource getSource() {
        return source;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public UtmTracking getUtm() {
        return utm;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
