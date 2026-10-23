package com.ats.job.application.usecase;

import com.ats.job.application.command.UpdateJobCommand;
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
import com.ats.job.application.service.JobReferenceValidator;
import com.ats.job.domain.valueobject.JobDetails;

/** Edit a requisition (Draft, Pending Approval, or Published with BRULE-13). */
@Service
public class UpdateJobUseCase {

    private final JobRepository jobRepository;
    private final JobLoader jobLoader;
    private final JobAccessPolicy accessPolicy;
    private final JobReferenceValidator referenceValidator;
    private final JobResponseAssembler assembler;
    private final BusinessClock clock;

    public UpdateJobUseCase(JobRepository jobRepository, JobLoader jobLoader, JobAccessPolicy accessPolicy,
                            JobReferenceValidator referenceValidator, JobResponseAssembler assembler,
                            BusinessClock clock) {
        this.jobRepository = jobRepository;
        this.jobLoader = jobLoader;
        this.accessPolicy = accessPolicy;
        this.referenceValidator = referenceValidator;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Transactional
    public JobResponse execute(UpdateJobCommand command, CurrentUser user) {
        Job job = jobLoader.load(command.id());
        accessPolicy.ensureCanManage(job, user);
        JobDetails details = command.form().toDetails();
        referenceValidator.validate(details);

        job.revise(details, user.actor(), clock.today(), clock.now());
        return assembler.toResponse(jobRepository.save(job));
    }
}
