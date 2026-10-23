package com.ats.job.application.usecase;

import com.ats.job.application.response.JobResponse;
import com.ats.job.application.service.JobAccessPolicy;
import com.ats.job.application.service.JobLoader;
import com.ats.job.application.service.JobResponseAssembler;
import com.ats.job.domain.aggregate.Job;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetJobUseCase {

    private final JobLoader jobLoader;
    private final JobAccessPolicy accessPolicy;
    private final JobResponseAssembler assembler;

    public GetJobUseCase(JobLoader jobLoader, JobAccessPolicy accessPolicy, JobResponseAssembler assembler) {
        this.jobLoader = jobLoader;
        this.accessPolicy = accessPolicy;
        this.assembler = assembler;
    }

    @Transactional(readOnly = true)
    public JobResponse execute(Long id, CurrentUser user) {
        Job job = jobLoader.load(id);
        accessPolicy.ensureCanView(job, user);
        return assembler.toResponse(job);
    }
}
