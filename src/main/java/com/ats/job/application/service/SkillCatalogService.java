package com.ats.job.application.service;

import com.ats.job.application.contract.SkillCatalog;
import com.ats.job.application.contract.SkillInfo;
import com.ats.job.domain.aggregate.Skill;
import com.ats.job.domain.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SkillCatalogService implements SkillCatalog {

    private final SkillRepository skillRepository;

    public SkillCatalogService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillInfo> findAll() {
        return skillRepository.findAll().stream().map(SkillCatalogService::toInfo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, SkillInfo> findByIds(Collection<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Map.of();
        }
        return skillRepository.findByIds(skillIds).stream()
                .map(SkillCatalogService::toInfo)
                .collect(Collectors.toMap(SkillInfo::id, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findExistingIds(Collection<Long> skillIds) {
        return skillIds == null || skillIds.isEmpty() ? Set.of() : skillRepository.findExistingIds(skillIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findIdsByNameContaining(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Set.of();
        }
        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        return skillRepository.findAll().stream()
                .filter(skill -> skill.getName().toLowerCase(Locale.ROOT).contains(needle))
                .map(Skill::getId)
                .collect(Collectors.toSet());
    }

    private static SkillInfo toInfo(Skill skill) {
        return new SkillInfo(skill.getId(), skill.getName(), skill.getCategory());
    }
}
