package com.ats.job.infrastructure.persistence.repository;

import com.ats.job.domain.aggregate.Skill;
import com.ats.job.domain.repository.SkillRepository;
import com.ats.job.infrastructure.persistence.converter.SkillPersistenceConverter;
import com.ats.job.infrastructure.persistence.mapper.SkillMyBatisMapper;
import com.ats.job.infrastructure.persistence.record.SkillRecord;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MyBatisSkillRepository implements SkillRepository {

    private final SkillMyBatisMapper mapper;

    public MyBatisSkillRepository(SkillMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Skill save(Skill skill) {
        SkillRecord record = SkillPersistenceConverter.toRecord(skill);
        mapper.insert(record);
        return SkillPersistenceConverter.toAggregate(record);
    }

    @Override
    public Optional<Skill> findByNameIgnoreCase(String name) {
        return mapper.findActiveByLowerName(name.trim().toLowerCase(Locale.ROOT))
                .map(SkillPersistenceConverter::toAggregate);
    }

    @Override
    public List<Skill> findAll() {
        return mapper.findAllActive().stream().map(SkillPersistenceConverter::toAggregate).toList();
    }

    @Override
    public List<Skill> findByIds(Collection<Long> ids) {
        return ids.isEmpty() ? List.of()
                : mapper.findActiveByIds(ids).stream().map(SkillPersistenceConverter::toAggregate).toList();
    }

    @Override
    public Set<Long> findExistingIds(Collection<Long> ids) {
        return findByIds(ids).stream().map(Skill::getId).collect(Collectors.toSet());
    }
}
