package com.ats.job.domain.aggregate;

import com.ats.job.domain.enums.EmploymentType;
import com.ats.job.domain.enums.JobPriority;
import com.ats.job.domain.enums.JobStatus;
import com.ats.job.domain.exception.JobRuleViolation;
import com.ats.job.domain.valueobject.JobDetails;
import com.ats.job.domain.valueobject.SalaryRange;
import com.ats.shared.security.HtmlSanitizer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Job requisition / posting. Department, recruiter and hiring manager are referenced by id only.
 * <p>Status flow (BRULE-11): DRAFT -> [PENDING_APPROVAL -> approved DRAFT] -> PUBLISHED -> CLOSED -> DRAFT.
 * An approved draft is "ready to publish"; changing title, department, employment type or hiring manager
 * withdraws the approval.
 */
public class Job {

    private static final LocalTime AUTO_CLOSE_TIME = LocalTime.of(23, 59);

    private final Long id;
    private final Long recruiterId;
    private JobDetails details;
    private JobStatus status;
    private Long approvedBy;
    private Instant approvedAt;
    private String rejectionReason;
    private Instant publishedAt;
    private Instant closedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;

    private Job(Long id, Long recruiterId, JobDetails details, JobStatus status, Long approvedBy, Instant approvedAt,
                String rejectionReason, Instant publishedAt, Instant closedAt, Instant createdAt, Instant updatedAt,
                String createdBy, String updatedBy, boolean deleted, Instant deletedAt) {
        this.id = id;
        this.recruiterId = Objects.requireNonNull(recruiterId, "Recruiter is required");
        this.details = Objects.requireNonNull(details, "Job details are required");
        this.status = Objects.requireNonNull(status, "Job status is required");
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.publishedAt = publishedAt;
        this.closedAt = closedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    /** "Save Draft": only the title is required; other values are validated when present. */
    public static Job createDraft(JobDetails details, Long recruiterId, String actor, LocalDate today, Instant now) {
        validateDraft(details, today);
        return new Job(null, recruiterId, details, JobStatus.DRAFT, null, null, null, null, null, now, now,
                actor, actor, false, null);
    }

    public static Job restore(Long id, Long recruiterId, JobDetails details, JobStatus status, Long approvedBy,
                              Instant approvedAt, String rejectionReason, Instant publishedAt, Instant closedAt,
                              Instant createdAt, Instant updatedAt, String createdBy, String updatedBy,
                              boolean deleted, Instant deletedAt) {
        return new Job(id, recruiterId, details, status, approvedBy, approvedAt, rejectionReason, publishedAt,
                closedAt, createdAt, updatedAt, createdBy, updatedBy, deleted, deletedAt);
    }

    /** Edit the requisition. Closed jobs cannot be edited; published jobs keep their critical fields (BRULE-13). */
    public void revise(JobDetails newDetails, String actor, LocalDate today, Instant now) {
        if (status == JobStatus.CLOSED) {
            throw JobRuleViolation.actionNotAllowed("edit", status);
        }
        validateDraft(newDetails, today);
        boolean criticalChanged = !Objects.equals(details.title(), newDetails.title())
                || !Objects.equals(details.departmentId(), newDetails.departmentId())
                || details.employmentType() != newDetails.employmentType();
        if (status == JobStatus.PUBLISHED) {
            if (criticalChanged) {
                throw JobRuleViolation.criticalFieldsLocked();
            }
            ensureComplete(newDetails, today);
        }
        if (criticalChanged || !Objects.equals(details.hiringManagerId(), newDetails.hiringManagerId())) {
            withdrawApproval();
        }
        this.details = newDetails;
        touch(actor, now);
    }

    /** "Save & Submit": validates every required field, then waits for the Hiring Manager when needed. */
    public void submit(String actor, LocalDate today, Instant now) {
        requireStatus("submit", JobStatus.DRAFT);
        ensureComplete(details, today);
        rejectionReason = null;
        if (details.requiresApproval() && approvedAt == null) {
            status = JobStatus.PENDING_APPROVAL;
        }
        touch(actor, now);
    }

    public void approve(Long hiringManagerId, String actor, Instant now) {
        requireStatus("approve", JobStatus.PENDING_APPROVAL);
        status = JobStatus.DRAFT;
        approvedBy = hiringManagerId;
        approvedAt = now;
        rejectionReason = null;
        touch(actor, now);
    }

    /** BRULE-11: a rejected requisition goes back to Draft with the reason. */
    public void reject(String reason, String actor, Instant now) {
        requireStatus("reject", JobStatus.PENDING_APPROVAL);
        if (reason == null || reason.isBlank()) {
            throw JobRuleViolation.rejectionReasonRequired();
        }
        status = JobStatus.DRAFT;
        withdrawApproval();
        rejectionReason = reason.trim();
        touch(actor, now);
    }

    public void publish(String actor, LocalDate today, Instant now) {
        if (status == JobStatus.PENDING_APPROVAL) {
            throw JobRuleViolation.approvalRequired();
        }
        requireStatus("publish", JobStatus.DRAFT);
        ensureComplete(details, today);
        if (details.requiresApproval() && approvedAt == null) {
            throw JobRuleViolation.approvalRequired();
        }
        status = JobStatus.PUBLISHED;
        publishedAt = now;
        closedAt = null;
        touch(actor, now);
    }

    /** Removes the job from the Career Site; existing applications are kept. */
    public void unpublish(String actor, Instant now) {
        requireStatus("unpublish", JobStatus.PUBLISHED);
        status = JobStatus.DRAFT;
        publishedAt = null;
        touch(actor, now);
    }

    public void close(String actor, Instant now) {
        if (status == JobStatus.CLOSED) {
            throw JobRuleViolation.actionNotAllowed("close", status);
        }
        status = JobStatus.CLOSED;
        closedAt = now;
        touch(actor, now);
    }

    /** BRULE-10: a published job closes at 23:59 (business time) on its deadline date. Returns true when closed. */
    public boolean closeIfExpired(LocalDateTime businessNow, String actor, Instant now) {
        if (status == JobStatus.PUBLISHED && details.deadline() != null
                && !businessNow.isBefore(details.deadline().atTime(AUTO_CLOSE_TIME))) {
            close(actor, now);
            return true;
        }
        return false;
    }

    /** A reopened job goes back to Draft and must be published again. */
    public void reopen(String actor, Instant now) {
        requireStatus("reopen", JobStatus.CLOSED);
        status = JobStatus.DRAFT;
        closedAt = null;
        publishedAt = null;
        touch(actor, now);
    }

    public void markDeleted(long applicationCount, String actor, Instant now) {
        if (status != JobStatus.DRAFT || applicationCount > 0) {
            throw JobRuleViolation.cannotDelete();
        }
        deleted = true;
        deletedAt = now;
        touch(actor, now);
    }

    /** BRULE-10: published and the deadline (inclusive) has not passed. */
    public boolean isAcceptingApplications(LocalDate today) {
        return !deleted && status == JobStatus.PUBLISHED
                && (details.deadline() == null || !today.isAfter(details.deadline()));
    }

    /** Complete, approved when needed, and still a draft. */
    public boolean isReadyToPublish(LocalDate today) {
        return status == JobStatus.DRAFT && missingFields(details).isEmpty()
                && (details.deadline() == null || !details.deadline().isBefore(today))
                && (!details.requiresApproval() || approvedAt != null);
    }

    public boolean isManagedBy(Long userId) {
        return recruiterId.equals(userId);
    }

    private void withdrawApproval() {
        approvedBy = null;
        approvedAt = null;
    }

    private void requireStatus(String action, JobStatus expected) {
        if (status != expected) {
            throw JobRuleViolation.actionNotAllowed(action, status);
        }
    }

    private void touch(String actor, Instant now) {
        updatedAt = now;
        updatedBy = actor;
    }

    private static void validateDraft(JobDetails details, LocalDate today) {
        if (details.title() == null) {
            throw JobRuleViolation.missingRequiredFields(List.of("title"));
        }
        if (details.deadline() != null && details.deadline().isBefore(today)) {
            throw JobRuleViolation.deadlineInPast();
        }
    }

    private static void ensureComplete(JobDetails details, LocalDate today) {
        List<String> missing = missingFields(details);
        if (!missing.isEmpty()) {
            throw JobRuleViolation.missingRequiredFields(missing);
        }
        if (details.deadline() != null && details.deadline().isBefore(today)) {
            throw JobRuleViolation.deadlineInPast();
        }
    }

    private static List<String> missingFields(JobDetails details) {
        List<String> missing = new ArrayList<>();
        if (details.title() == null) {
            missing.add("title");
        }
        if (details.departmentId() == null) {
            missing.add("departmentId");
        }
        String text = HtmlSanitizer.toPlainText(details.description());
        if (text == null || text.isBlank()) {
            missing.add("description");
        }
        if (details.skillIds().isEmpty()) {
            missing.add("skillIds");
        }
        if (details.hiringManagerId() == null) {
            missing.add("hiringManagerId");
        }
        return missing;
    }

    public Long getId() {
        return id;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public JobDetails getDetails() {
        return details;
    }

    public String getTitle() {
        return details.title();
    }

    public Long getDepartmentId() {
        return details.departmentId();
    }

    public EmploymentType getEmploymentType() {
        return details.employmentType();
    }

    public SalaryRange getSalary() {
        return details.salary();
    }

    public Set<Long> getSkillIds() {
        return details.skillIds();
    }

    public Long getHiringManagerId() {
        return details.hiringManagerId();
    }

    public LocalDate getDeadline() {
        return details.deadline();
    }

    public JobPriority getPriority() {
        return details.priority();
    }

    public JobStatus getStatus() {
        return status;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
