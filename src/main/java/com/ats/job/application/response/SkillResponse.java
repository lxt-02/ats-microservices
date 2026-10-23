package com.ats.job.application.response;

import com.ats.job.application.contract.SkillInfo;
import com.ats.job.domain.aggregate.Skill;

public record SkillResponse(Long id, String name, String category) {

    public static SkillResponse from(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName(), skill.getCategory());
    }

    public static SkillResponse from(SkillInfo skill) {
        return new SkillResponse(skill.id(), skill.name(), skill.category());
    }
}
