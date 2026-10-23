package com.ats.job.domain.repository;

import com.ats.job.domain.aggregate.Skill;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SkillRepository {

    Skill save(Skill skill);

    Optional<Skill> findByNameIgnoreCase(String name);

    List<Skill> findAll();

    List<Skill> findByIds(Collection<Long> ids);

    Set<Long> findExistingIds(Collection<Long> ids);
}
