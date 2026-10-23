package com.ats.candidate.infrastructure.persistence.converter;

import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.enums.ApplicationSource;
import com.ats.candidate.domain.enums.ApplicationStatus;
import com.ats.candidate.domain.valueobject.HiringDecision;
import com.ats.candidate.domain.valueobject.StageTransition;
import com.ats.candidate.domain.valueobject.UtmTracking;
import com.ats.candidate.infrastructure.persistence.record.JobApplicationRecord;
import com.ats.candidate.infrastructure.persistence.record.StageTransitionRecord;

public final class JobApplicationPersistenceConverter {

    private JobApplicationPersistenceConverter() {
    }

    public static JobApplicationRecord toRecord(JobApplication application) {
        JobApplicationRecord record = new JobApplicationRecord();
        record.setId(application.getId());
        record.setJobId(application.getJobId());
        record.setCandidateId(application.getCandidateId());
        record.setCvId(application.getCvId());
        record.setStatus(application.getStatus().name());
        record.setCoverLetter(application.getCoverLetter());
        record.setSource(application.getSource() == null ? null : application.getSource().name());
        record.setReferralCode(application.getReferralCode());
        record.setUtmSource(application.getUtm().source());
        record.setUtmMedium(application.getUtm().medium());
        record.setUtmCampaign(application.getUtm().campaign());
        record.setTransferredFromId(application.getTransferredFromId());
        HiringDecision decision = application.getHiringDecision();
        if (decision != null) {
            record.setHmApproved(decision.approved());
            record.setAssignedDepartmentId(decision.assignedDepartmentId());
            record.setDecisionComment(decision.comment());
            record.setDecidedBy(decision.decidedBy());
            record.setDecidedAt(decision.decidedAt());
        }
        record.setAppliedAt(application.getAppliedAt());
        record.setStatusChangedAt(application.getStatusChangedAt());
        record.setCreatedAt(application.getAppliedAt());
        record.setUpdatedAt(application.getUpdatedAt());
        record.setCreatedBy(application.getCreatedBy());
        record.setUpdatedBy(application.getUpdatedBy());
        return record;
    }

    public static JobApplication toAggregate(JobApplicationRecord record) {
        HiringDecision decision = record.getHmApproved() == null ? null
                : new HiringDecision(record.getHmApproved(), record.getAssignedDepartmentId(),
                record.getDecisionComment(), record.getDecidedBy(), record.getDecidedAt());
        return JobApplication.restore(record.getId(), record.getJobId(), record.getCandidateId(), record.getCvId(),
                ApplicationStatus.valueOf(record.getStatus()), record.getCoverLetter(),
                record.getSource() == null ? null : ApplicationSource.valueOf(record.getSource()),
                record.getReferralCode(),
                new UtmTracking(record.getUtmSource(), record.getUtmMedium(), record.getUtmCampaign()),
                record.getTransferredFromId(), decision, record.getAppliedAt(), record.getStatusChangedAt(),
                record.getCreatedBy(), record.getUpdatedAt(), record.getUpdatedBy());
    }

    public static StageTransitionRecord toRecord(Long applicationId, StageTransition transition) {
        StageTransitionRecord record = new StageTransitionRecord();
        record.setId(transition.id());
        record.setApplicationId(applicationId);
        record.setFromStatus(transition.fromStatus() == null ? null : transition.fromStatus().name());
        record.setToStatus(transition.toStatus().name());
        record.setNotes(transition.note());
        record.setMovedAt(transition.movedAt());
        record.setCreatedBy(transition.movedBy());
        return record;
    }

    public static StageTransition toTransition(StageTransitionRecord record) {
        return new StageTransition(record.getId(),
                record.getFromStatus() == null ? null : ApplicationStatus.valueOf(record.getFromStatus()),
                ApplicationStatus.valueOf(record.getToStatus()), record.getNotes(), record.getCreatedBy(),
                record.getMovedAt());
    }
}
