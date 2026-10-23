package com.ats.job.infrastructure.persistence.converter;

import com.ats.job.domain.aggregate.Skill;
import com.ats.job.infrastructure.persistence.record.SkillRecord;

public final class SkillPersistenceConverter {

    private SkillPersistenceConverter() {
    }

    public static SkillRecord toRecord(Skill skill) {
        SkillRecord record = new SkillRecord();
        record.setId(skill.getId());
        record.setSkillName(skill.getName());
        record.setCategory(skill.getCategory());
        record.setCreatedAt(skill.getCreatedAt());
        record.setUpdatedAt(skill.getCreatedAt());
        record.setCreatedBy(skill.getCreatedBy());
        record.setUpdatedBy(skill.getCreatedBy());
        return record;
    }

    public static Skill toAggregate(SkillRecord record) {
        return Skill.restore(record.getId(), record.getSkillName(), record.getCategory(), record.getCreatedAt(),
                record.getCreatedBy());
    }
}
