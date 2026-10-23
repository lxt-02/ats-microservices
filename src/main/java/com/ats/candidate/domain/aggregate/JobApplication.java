package com.ats.candidate.domain.aggregate;

import com.ats.candidate.domain.enums.ApplicationSource;
import com.ats.candidate.domain.enums.ApplicationStatus;
import com.ats.candidate.domain.exception.CandidateRuleViolation;
import com.ats.candidate.domain.valueobject.HiringDecision;
import com.ats.candidate.domain.valueobject.StageTransition;
import com.ats.candidate.domain.valueobject.UtmTracking;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A candidate's application to a job (job referenced by id) and its position in the recruitment pipeline.
 * Every state change is recorded as a {@link StageTransition} (audit trail), saved with the application.
 */
public class JobApplication {

    private static final int MAX_NOTE_LENGTH = 2000;

    private final Long id;
    private final Long jobId;
    private final Long candidateId;
    private final Long cvId;
    private ApplicationStatus status;
    private final String coverLetter;
    private final ApplicationSource source;
    private final String referralCode;
    private final UtmTracking utm;
    private final Long transferredFromId;
    private HiringDecision hiringDecision;
    private final Instant appliedAt;
    private Instant statusChangedAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    private final List<StageTransition> newTransitions = new ArrayList<>();

    private JobApplication(Long id, Long jobId, Long candidateId, Long cvId, ApplicationStatus status,
                           String coverLetter, ApplicationSource source, String referralCode, UtmTracking utm,
                           Long transferredFromId, HiringDecision hiringDecision, Instant appliedAt,
                           Instant statusChangedAt, String createdBy, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.jobId = Objects.requireNonNull(jobId, "Job is required");
        this.candidateId = Objects.requireNonNull(candidateId, "Candidate is required");
        this.cvId = cvId;
        this.status = Objects.requireNonNull(status, "Application status is required");
        this.coverLetter = coverLetter == null || coverLetter.isBlank() ? null : coverLetter.trim();
        this.source = source;
        this.referralCode = referralCode == null || referralCode.isBlank() ? null : referralCode.trim();
        this.utm = utm == null ? UtmTracking.none() : utm;
        this.transferredFromId = transferredFromId;
        this.hiringDecision = hiringDecision;
        this.appliedAt = appliedAt;
        this.statusChangedAt = statusChangedAt == null ? appliedAt : statusChangedAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    /** A new application, in status NEW. */
    public static JobApplication submit(Long jobId, Long candidateId, Long cvId, String coverLetter,
                                        ApplicationSource source, String referralCode, UtmTracking utm,
                                        String actor, Instant now) {
        JobApplication application = new JobApplication(null, jobId, candidateId, cvId, ApplicationStatus.NEW,
                coverLetter, source, referralCode, utm, null, null, now, now, actor, now, actor);
        application.newTransitions.add(new StageTransition(null, null, ApplicationStatus.NEW, null, actor, now));
        return application;
    }

    public static JobApplication restore(Long id, Long jobId, Long candidateId, Long cvId, ApplicationStatus status,
                                         String coverLetter, ApplicationSource source, String referralCode,
                                         UtmTracking utm, Long transferredFromId, HiringDecision hiringDecision,
                                         Instant appliedAt, Instant statusChangedAt, String createdBy,
                                         Instant updatedAt, String updatedBy) {
        return new JobApplication(id, jobId, candidateId, cvId, status, coverLetter, source, referralCode, utm,
                transferredFromId, hiringDecision, appliedAt, statusChangedAt, createdBy, updatedAt, updatedBy);
    }

    /**
     * A recruiter moves the card (Kanban drag &amp; drop, CV screening pass/reject, interview stages, offer outcome).
     * OFFERED needs the hiring manager's decision and TRANSFERRED needs a target job, so they have their own
     * methods. A rejection needs no reason here (the confirmation is on screen, MSG-PIPE-003).
     */
    public void moveTo(ApplicationStatus target, String note, String actor, Instant now) {
        if (target == ApplicationStatus.OFFERED || target == ApplicationStatus.TRANSFERRED) {
            throw CandidateRuleViolation.invalidTransition(status, target);
        }
        changeStatus(target, note, actor, now);
    }

    /** UC-03.3 / UC-03.4: the hiring manager approves (assigning a team, state Offered) or rejects. */
    public void decideAfterHmInterview(boolean approve, Long departmentId, String comment, Long hiringManagerId,
                                       String actor, Instant now) {
        if (status != ApplicationStatus.HM_INTERVIEW) {
            throw CandidateRuleViolation.invalidTransition(status,
                    approve ? ApplicationStatus.OFFERED : ApplicationStatus.REJECTED);
        }
        String cleanComment = clean(comment);
        if (approve && departmentId == null) {
            throw CandidateRuleViolation.targetTeamRequired();
        }
        if (!approve && cleanComment == null) {
            throw CandidateRuleViolation.reasonRequired();
        }
        hiringDecision = new HiringDecision(approve, approve ? departmentId : null, cleanComment, hiringManagerId, now);
        changeStatus(approve ? ApplicationStatus.OFFERED : ApplicationStatus.REJECTED, cleanComment, actor, now);
    }

    /** UC-02.18 Update Offer Status. */
    public void recordOfferOutcome(boolean accepted, String note, String actor, Instant now) {
        changeStatus(accepted ? ApplicationStatus.HIRED : ApplicationStatus.OFFER_DECLINED, note, actor, now);
    }

    /**
     * BRULE-04: a transfer is allowed while the application is in Round 1 (New, CV Screening) or was rejected
     * during Round 1. {@code rejectedFrom} is the state before the rejection, when rejected.
     */
    public boolean canTransfer(ApplicationStatus rejectedFrom) {
        return status.isScreeningRound()
                || (status == ApplicationStatus.REJECTED && rejectedFrom != null && rejectedFrom.isScreeningRound());
    }

    /** Closes this application as TRANSFERRED; the caller creates the application on the target job. */
    public void markTransferred(ApplicationStatus rejectedFrom, String note, String actor, Instant now) {
        if (!canTransfer(rejectedFrom)) {
            throw CandidateRuleViolation.transferNotAllowed();
        }
        recordTransition(ApplicationStatus.TRANSFERRED, note, actor, now);
    }

    /** The application on the target job: status NEW, same candidate and CV, linked to its source. */
    public JobApplication transferTo(Long targetJobId, String note, String actor, Instant now) {
        JobApplication copy = new JobApplication(null, targetJobId, candidateId, cvId, ApplicationStatus.NEW,
                coverLetter, source, referralCode, utm, id, null, now, now, actor, now, actor);
        copy.newTransitions.add(new StageTransition(null, null, ApplicationStatus.NEW,
                clean(note) == null ? "Transferred from job " + jobId : clean(note), actor, now));
        return copy;
    }

    /**
     * BRULE-12 explicit Reopen: a rejected or declined application goes back to the state it had before.
     * The HM decision is cleared.
     */
    public void reopen(ApplicationStatus previousStatus, String note, String actor, Instant now) {
        if ((status != ApplicationStatus.REJECTED && status != ApplicationStatus.OFFER_DECLINED)
                || previousStatus == null || previousStatus.isTerminal()) {
            throw CandidateRuleViolation.reopenNotAllowed(status);
        }
        hiringDecision = null;
        recordTransition(previousStatus, note, actor, now);
    }

    /** Audit records not saved yet; the repository persists them and then calls {@link #clearNewTransitions()}. */
    public List<StageTransition> getNewTransitions() {
        return List.copyOf(newTransitions);
    }

    public void clearNewTransitions() {
        newTransitions.clear();
    }

    /** Application ID shown to candidates: APP-YYYY-NNNNN. */
    public static String code(Long id, Instant appliedAt, ZoneId zone) {
        return String.format("APP-%d-%05d", appliedAt.atZone(zone).getYear(), id);
    }

    private void changeStatus(ApplicationStatus target, String note, String actor, Instant now) {
        if (!status.canMoveTo(target)) {
            throw CandidateRuleViolation.invalidTransition(status, target);
        }
        recordTransition(target, note, actor, now);
    }

    private void recordTransition(ApplicationStatus target, String note, String actor, Instant now) {
        String cleanNote = clean(note);
        if (cleanNote != null && cleanNote.length() > MAX_NOTE_LENGTH) {
            cleanNote = cleanNote.substring(0, MAX_NOTE_LENGTH);
        }
        newTransitions.add(new StageTransition(null, status, target, cleanNote, actor, now));
        status = target;
        statusChangedAt = now;
        updatedAt = now;
        updatedBy = actor;
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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

    public Long getTransferredFromId() {
        return transferredFromId;
    }

    public HiringDecision getHiringDecision() {
        return hiringDecision;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public Instant getStatusChangedAt() {
        return statusChangedAt;
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
