package com.ats.candidate.infrastructure.persistence.converter;

import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.enums.CandidateStatus;
import com.ats.candidate.domain.valueobject.UtmTracking;
import com.ats.candidate.infrastructure.persistence.record.CandidateRecord;

import java.util.Set;

public final class CandidatePersistenceConverter {

    private CandidatePersistenceConverter() {
    }

    public static CandidateRecord toRecord(Candidate candidate) {
        CandidateRecord record = new CandidateRecord();
        record.setId(candidate.getId());
        record.setUserId(candidate.getUserId());
        record.setFullName(candidate.getFullName());
        record.setEmail(candidate.getEmail());
        record.setPhone(candidate.getPhone());
        record.setPhoneKey(phoneKey(candidate.getPhone()));
        record.setStatus(candidate.getStatus().name());
        record.setSource(candidate.getSource());
        record.setUtmSource(candidate.getUtm().source());
        record.setUtmMedium(candidate.getUtm().medium());
        record.setUtmCampaign(candidate.getUtm().campaign());
        record.setDuplicate(candidate.isDuplicate());
        record.setDuplicateOfId(candidate.getDuplicateOfId());
        record.setCreatedAt(candidate.getCreatedAt());
        record.setUpdatedAt(candidate.getUpdatedAt());
        record.setCreatedBy(candidate.getCreatedBy());
        record.setUpdatedBy(candidate.getUpdatedBy());
        record.setDeleted(candidate.isDeleted());
        record.setDeletedAt(candidate.getDeletedAt());
        return record;
    }

    /** Last 9 digits, or null when the phone has fewer than 9 digits. */
    public static String phoneKey(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        return digits.length() < 9 ? null : digits.substring(digits.length() - 9);
    }

    public static Candidate toAggregate(CandidateRecord record, Set<Long> skillIds) {
        return Candidate.restore(record.getId(), record.getUserId(), record.getFullName(), record.getEmail(),
                record.getPhone(), CandidateStatus.valueOf(record.getStatus()), record.getSource(),
                new UtmTracking(record.getUtmSource(), record.getUtmMedium(), record.getUtmCampaign()),
                record.isDuplicate(), record.getDuplicateOfId(), skillIds, record.getCreatedAt(),
                record.getUpdatedAt(), record.getCreatedBy(), record.getUpdatedBy(), record.isDeleted(),
                record.getDeletedAt());
    }
}
