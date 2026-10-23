package com.ats.job.application.usecase;

import com.ats.job.application.response.JobResponse;
import com.ats.job.application.service.BusinessClock;
import com.ats.job.application.service.JobAccessPolicy;
import com.ats.job.application.service.JobLoader;
import com.ats.job.application.service.JobResponseAssembler;
import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.repository.JobRepository;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-02.2 Publish Job Posting (BRULE-11: approval first when required). */
@Service
public class PublishJobUseCase {

    private final JobRepository jobRepository;
    private final JobLoader jobLoader;
    private final JobAccessPolicy accessPolicy;
    private final JobResponseAssembler assembler;
    private final BusinessClock clock;

    public PublishJobUseCase(JobRepository jobRepository, JobLoader jobLoader, JobAccessPolicy accessPolicy,
            JobResponseAssembler assembler, BusinessClock clock) {
        this.jobRepository = jobRepository;
        this.jobLoader = jobLoader;
        this.accessPolicy = accessPolicy;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Transactional
    public JobResponse execute(Long id, CurrentUser user) {
        Job job = jobLoader.load(id);
        accessPolicy.ensureCanManage(job, user);
        job.publish(user.actor(), clock.today(), clock.now());
        return assembler.toResponse(jobRepository.save(job));
    }
}
