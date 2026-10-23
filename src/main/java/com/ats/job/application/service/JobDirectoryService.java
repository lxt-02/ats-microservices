package com.ats.job.application.service;

import com.ats.job.application.contract.JobDirectory;
import com.ats.job.application.contract.JobInfo;
import com.ats.job.application.port.out.JobReadQuery;
import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JobDirectoryService implements JobDirectory {

    private final JobRepository jobRepository;
    private final JobReadQuery jobReadQuery;
    private final BusinessClock clock;

    public JobDirectoryService(JobRepository jobRepository, JobReadQuery jobReadQuery, BusinessClock clock) {
        this.jobRepository = jobRepository;
        this.jobReadQuery = jobReadQuery;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobInfo> findById(Long jobId) {
        LocalDate today = clock.today();
        return jobId == null ? Optional.empty() : jobRepository.findById(jobId).map(job -> toInfo(job, today));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, JobInfo> findByIds(Collection<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            return Map.of();
        }
        LocalDate today = clock.today();
        return jobRepository.findByIds(jobIds.stream().filter(Objects::nonNull).distinct().toList()).stream()
                .map(job -> toInfo(job, today))
                .collect(Collectors.toMap(JobInfo::id, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findJobIdsAssignedTo(Long userId) {
        return userId == null ? Set.of() : jobReadQuery.findJobIdsAssignedTo(userId);
    }

    private static JobInfo toInfo(Job job, LocalDate today) {
        return new JobInfo(job.getId(), job.getTitle(), job.getDepartmentId(), job.getStatus().name(),
                job.getRecruiterId(), job.getHiringManagerId(), job.getDeadline(), job.isAcceptingApplications(today));
    }
}
