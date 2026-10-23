package com.ats.candidate.infrastructure.persistence.converter;

import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.enums.ApplicationSource;
import com.ats.candidate.domain.enums.ApplicationStatus;
import com.ats.candidate.domain.valueobject.UtmTracking;
import com.ats.candidate.infrastructure.persistence.record.JobApplicationRecord;

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
        record.setAppliedAt(application.getAppliedAt());
        record.setCreatedAt(application.getAppliedAt());
        record.setUpdatedAt(application.getUpdatedAt());
        record.setCreatedBy(application.getCreatedBy());
        record.setUpdatedBy(application.getUpdatedBy());
        return record;
    }

    public static JobApplication toAggregate(JobApplicationRecord record) {
        return JobApplication.restore(record.getId(), record.getJobId(), record.getCandidateId(), record.getCvId(),
                ApplicationStatus.valueOf(record.getStatus()), record.getCoverLetter(),
                record.getSource() == null ? null : ApplicationSource.valueOf(record.getSource()),
                record.getReferralCode(),
                new UtmTracking(record.getUtmSource(), record.getUtmMedium(), record.getUtmCampaign()),
                record.getAppliedAt(), record.getCreatedBy(), record.getUpdatedAt(), record.getUpdatedBy());
    }
}
