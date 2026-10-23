package com.ats.job.application.usecase;

import com.ats.job.application.command.CreateSkillCommand;
import com.ats.job.application.response.SkillResponse;
import com.ats.job.application.service.BusinessClock;
import com.ats.job.domain.aggregate.Skill;
import com.ats.job.domain.repository.SkillRepository;
import com.ats.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSkillUseCase {

    private final SkillRepository skillRepository;
    private final BusinessClock clock;

    public CreateSkillUseCase(SkillRepository skillRepository, BusinessClock clock) {
        this.skillRepository = skillRepository;
        this.clock = clock;
    }

    @Transactional
    public SkillResponse execute(CreateSkillCommand command, String actor) {
        Skill skill = Skill.create(command.name(), command.category(), actor, clock.now());
        skillRepository.findByNameIgnoreCase(skill.getName()).ifPresent(existing -> {
            throw new ConflictException("Skill already exists: " + existing.getName());
        });
        return SkillResponse.from(skillRepository.save(skill));
    }
}
