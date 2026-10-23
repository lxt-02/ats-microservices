package com.ats.job.application.usecase;

import com.ats.job.application.response.SkillResponse;
import com.ats.job.domain.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/** Skill autocomplete: every skill, or those whose name contains {@code keyword}. */
@Service
public class ListSkillsUseCase {

    private final SkillRepository skillRepository;

    public ListSkillsUseCase(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> execute(String keyword) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return skillRepository.findAll().stream()
                .filter(skill -> needle.isEmpty() || skill.getName().toLowerCase(Locale.ROOT).contains(needle))
                .map(SkillResponse::from)
                .toList();
    }
}
