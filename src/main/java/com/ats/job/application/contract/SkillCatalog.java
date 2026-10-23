package com.ats.job.application.contract;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Skill master data for other modules (CV parsing, candidate skills). */
public interface SkillCatalog {

    List<SkillInfo> findAll();

    Map<Long, SkillInfo> findByIds(Collection<Long> skillIds);

    /** Which of {@code skillIds} exist. */
    Set<Long> findExistingIds(Collection<Long> skillIds);

    /** Skills whose name contains {@code keyword} (case-insensitive). */
    Set<Long> findIdsByNameContaining(String keyword);
}
