package com.ats.job.infrastructure.persistence.repository;

import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.repository.JobRepository;
import com.ats.job.infrastructure.persistence.converter.JobPersistenceConverter;
import com.ats.job.infrastructure.persistence.mapper.JobMyBatisMapper;
import com.ats.job.infrastructure.persistence.record.JobRecord;
import com.ats.job.infrastructure.persistence.view.JobSkillView;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MyBatisJobRepository implements JobRepository {

    private final JobMyBatisMapper mapper;

    public MyBatisJobRepository(JobMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Job save(Job job) {
        JobRecord record = JobPersistenceConverter.toRecord(job);
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.update(record);
            mapper.deleteSkills(record.getId());
        }
        if (!job.getSkillIds().isEmpty()) {
            mapper.insertSkills(record.getId(), job.getSkillIds());
        }
        return JobPersistenceConverter.toAggregate(record, job.getSkillIds());
    }

    @Override
    public Optional<Job> findById(Long id) {
        return mapper.findActiveById(id).map(record -> toAggregates(List.of(record)).get(0));
    }

    @Override
    public List<Job> findByIds(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : toAggregates(mapper.findActiveByIds(ids));
    }

    @Override
    public List<Job> findPublishedWithDeadlineOnOrBefore(LocalDate date) {
        return toAggregates(mapper.findPublishedWithDeadlineOnOrBefore(date));
    }

    private List<Job> toAggregates(List<JobRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        Map<Long, Set<Long>> skills = mapper.findSkillIdsByJobIds(records.stream().map(JobRecord::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(JobSkillView::getJobId,
                        Collectors.mapping(JobSkillView::getSkillId, Collectors.toSet())));
        return records.stream()
                .map(record -> JobPersistenceConverter.toAggregate(record, skills.getOrDefault(record.getId(), Set.of())))
                .toList();
    }
}
