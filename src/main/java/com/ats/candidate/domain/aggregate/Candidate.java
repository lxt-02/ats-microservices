package com.ats.candidate.domain.aggregate;

import com.ats.candidate.domain.enums.CandidateStatus;
import com.ats.candidate.domain.valueobject.ParsedCvData;
import com.ats.candidate.domain.valueobject.UtmTracking;

import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * A person in the talent pool. Created when someone applies on the Career Site (identified by email) or when a
 * recruiter imports a CV (name and email come later from parsing). Skills reference the Job module's skill
 * master data by id. {@code duplicateOfId} flags a probable duplicate (same email or phone as another profile).
 */
public class Candidate {

    private final Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private CandidateStatus status;
    private String source;
    private UtmTracking utm;
    private boolean duplicate;
    private Long duplicateOfId;
    private Set<Long> skillIds;
    private final Instant createdAt;
    private Instant updatedAt;
    private final String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;

    private Candidate(Long id, Long userId, String fullName, String email, String phone, CandidateStatus status,
                      String source, UtmTracking utm, boolean duplicate, Long duplicateOfId, Set<Long> skillIds,
                      Instant createdAt, Instant updatedAt, String createdBy, String updatedBy, boolean deleted,
                      Instant deletedAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = clean(fullName);
        this.email = normalizeEmail(email);
        this.phone = clean(phone);
        this.status = Objects.requireNonNull(status, "Candidate status is required");
        this.source = clean(source);
        this.utm = utm == null ? UtmTracking.none() : utm;
        this.duplicate = duplicate;
        this.duplicateOfId = duplicateOfId;
        this.skillIds = skillIds == null ? new HashSet<>() : new HashSet<>(skillIds);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    /** Career Site applicant: name and email are required. */
    public static Candidate registerApplicant(String fullName, String email, String phone, String source,
                                             UtmTracking utm, String actor, Instant now) {
        if (clean(fullName) == null) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (normalizeEmail(email) == null) {
            throw new IllegalArgumentException("Email is required");
        }
        return new Candidate(null, null, fullName, email, phone, CandidateStatus.ACTIVE, source, utm, false, null,
                Set.of(), now, now, actor, actor, false, null);
    }

    /** Recruiter CV import: the profile is filled in by the parser. */
    public static Candidate fromImportedCv(String actor, Instant now) {
        return new Candidate(null, null, null, null, null, CandidateStatus.ACTIVE, "IMPORT", UtmTracking.none(),
                false, null, Set.of(), now, now, actor, actor, false, null);
    }

    public static Candidate restore(Long id, Long userId, String fullName, String email, String phone,
                                    CandidateStatus status, String source, UtmTracking utm, boolean duplicate,
                                    Long duplicateOfId, Set<Long> skillIds, Instant createdAt, Instant updatedAt,
                                    String createdBy, String updatedBy, boolean deleted, Instant deletedAt) {
        return new Candidate(id, userId, fullName, email, phone, status, source, utm, duplicate, duplicateOfId,
                skillIds, createdAt, updatedAt, createdBy, updatedBy, deleted, deletedAt);
    }

    /** A returning applicant: keep the latest name and phone they typed. */
    public void refreshContact(String newFullName, String newPhone, String actor, Instant now) {
        if (clean(newFullName) != null) {
            fullName = clean(newFullName);
        }
        if (clean(newPhone) != null) {
            phone = clean(newPhone);
        }
        touch(actor, now);
    }

    /** Fills empty profile fields from the parsed CV and adds the recognised skills. */
    public void applyParsedCv(ParsedCvData data, Set<Long> recognisedSkillIds, String actor, Instant now) {
        if (fullName == null && data.fullName() != null) {
            fullName = clean(data.fullName());
        }
        if (email == null && data.email() != null) {
            email = normalizeEmail(data.email());
        }
        if (phone == null && data.phone() != null) {
            phone = clean(data.phone());
        }
        skillIds.addAll(recognisedSkillIds);
        touch(actor, now);
    }

    /** Manual completion by a recruiter (e.g. after a partial parse). */
    public void editProfile(String newFullName, String newEmail, String newPhone, Set<Long> newSkillIds,
                            String actor, Instant now) {
        fullName = clean(newFullName);
        email = normalizeEmail(newEmail);
        phone = clean(newPhone);
        skillIds = newSkillIds == null ? new HashSet<>() : new HashSet<>(newSkillIds);
        touch(actor, now);
    }

    public void markDuplicateOf(Long otherCandidateId, String actor, Instant now) {
        if (otherCandidateId == null || otherCandidateId.equals(id)) {
            return;
        }
        duplicate = true;
        duplicateOfId = otherCandidateId;
        touch(actor, now);
    }

    private void touch(String actor, Instant now) {
        updatedAt = now;
        updatedBy = actor;
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeEmail(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public String getSource() {
        return source;
    }

    public UtmTracking getUtm() {
        return utm;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public Long getDuplicateOfId() {
        return duplicateOfId;
    }

    public Set<Long> getSkillIds() {
        return Set.copyOf(skillIds);
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
