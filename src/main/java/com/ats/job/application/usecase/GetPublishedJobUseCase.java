package com.ats.job.application.usecase;

import com.ats.job.application.response.CareerJobDetailResponse;
import com.ats.job.application.response.DepartmentRef;
import com.ats.job.application.service.BusinessClock;
import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.enums.JobStatus;
import com.ats.job.domain.repository.JobRepository;
import com.ats.job.domain.repository.SkillRepository;
import com.ats.job.domain.valueobject.SalaryRange;
import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Public job detail page. Only published jobs are visible; an expired one shows as not accepting applications. */
@Service
public class GetPublishedJobUseCase {

    private static final int DEADLINE_WARNING_DAYS = 3;

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final DepartmentDirectory departmentDirectory;
    private final BusinessClock clock;

    public GetPublishedJobUseCase(JobRepository jobRepository, SkillRepository skillRepository,
                                  DepartmentDirectory departmentDirectory, BusinessClock clock) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
        this.departmentDirectory = departmentDirectory;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CareerJobDetailResponse execute(Long id) {
        Job job = jobRepository.findById(id)
                .filter(found -> found.getStatus() == JobStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        LocalDate today = clock.today();
        SalaryRange salary = job.getSalary();
        List<String> skills = skillRepository.findByIds(job.getSkillIds()).stream()
                .map(skill -> skill.getName()).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        String departmentName = departmentDirectory.findNames(Set.of(job.getDepartmentId()))
                .get(job.getDepartmentId());
        LocalDate deadline = job.getDeadline();
        boolean approaching = deadline != null && !today.isAfter(deadline)
                && !today.plusDays(DEADLINE_WARNING_DAYS).isBefore(deadline);
        return new CareerJobDetailResponse(
                job.getId(), job.getTitle(), new DepartmentRef(job.getDepartmentId(), departmentName),
                job.getDetails().location(), job.getEmploymentType().name(), job.getDetails().description(),
                skills,
                salary.isDisclosed() ? salary.min() : null,
                salary.isDisclosed() ? salary.max() : null,
                salary.isDisclosed() ? salary.currency().name() : null,
                deadline, approaching, job.isAcceptingApplications(today), job.getPublishedAt());
    }
}
