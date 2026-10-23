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

/** BRULE-11: the hiring manager rejects the requisition; it goes back to Draft with the reason. */
@Service
public class RejectJobUseCase {

    private final JobRepository jobRepository;
    private final JobLoader jobLoader;
    private final JobAccessPolicy accessPolicy;
    private final JobResponseAssembler assembler;
    private final BusinessClock clock;

    public RejectJobUseCase(JobRepository jobRepository, JobLoader jobLoader, JobAccessPolicy accessPolicy,
                            JobResponseAssembler assembler, BusinessClock clock) {
        this.jobRepository = jobRepository;
        this.jobLoader = jobLoader;
        this.accessPolicy = accessPolicy;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Transactional
    public JobResponse execute(Long id, String reason, CurrentUser user) {
        Job job = jobLoader.load(id);
        accessPolicy.ensureCanReview(job, user);
        job.reject(reason, user.actor(), clock.now());
        return assembler.toResponse(jobRepository.save(job));
    }
}
