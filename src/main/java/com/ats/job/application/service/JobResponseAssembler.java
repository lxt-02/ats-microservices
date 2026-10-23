package com.ats.job.application.service;

import com.ats.job.application.contract.JobApplicationCounter;
import com.ats.job.application.response.JobResponse;
import com.ats.job.application.response.SkillResponse;
import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.repository.SkillRepository;
import com.ats.organization.application.contract.DepartmentDirectory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Builds the job detail: department name, skill names and application count come from other places. */
@Component
public class JobResponseAssembler {

    private final DepartmentDirectory departmentDirectory;
    private final SkillRepository skillRepository;
    private final JobApplicationCounter applicationCounter;
    private final BusinessClock clock;

    public JobResponseAssembler(DepartmentDirectory departmentDirectory, SkillRepository skillRepository,
                                JobApplicationCounter applicationCounter, BusinessClock clock) {
        this.departmentDirectory = departmentDirectory;
        this.skillRepository = skillRepository;
        this.applicationCounter = applicationCounter;
        this.clock = clock;
    }

    public JobResponse toResponse(Job job) {
        String departmentName = job.getDepartmentId() == null ? null
                : departmentDirectory.findNames(Set.of(job.getDepartmentId())).get(job.getDepartmentId());
        List<SkillResponse> skills = job.getSkillIds().isEmpty() ? List.of()
                : skillRepository.findByIds(job.getSkillIds()).stream()
                .map(SkillResponse::from)
                .sorted(Comparator.comparing(SkillResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        long applications = job.getId() == null ? 0
                : applicationCounter.countByJobIds(Set.of(job.getId())).getOrDefault(job.getId(), 0L);
        return JobResponse.from(job, departmentName, skills, applications, job.isReadyToPublish(clock.today()));
    }

    public long applicationCount(Long jobId) {
        Map<Long, Long> counts = applicationCounter.countByJobIds(Set.of(jobId));
        return counts.getOrDefault(jobId, 0L);
    }
}
