package com.ats.job.application.usecase;

import com.ats.job.application.command.CreateJobCommand;
import com.ats.job.application.response.JobResponse;
import com.ats.job.application.service.BusinessClock;
import com.ats.job.application.service.JobReferenceValidator;
import com.ats.job.application.service.JobResponseAssembler;
import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.repository.JobRepository;
import com.ats.job.domain.valueobject.JobDetails;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-02.1 Save Draft. A recruiter is assigned to their own job; an admin picks the recruiter. */
@Service
public class CreateJobUseCase {

    private final JobRepository jobRepository;
    private final JobReferenceValidator referenceValidator;
    private final JobResponseAssembler assembler;
    private final BusinessClock clock;

    public CreateJobUseCase(JobRepository jobRepository, JobReferenceValidator referenceValidator,
                            JobResponseAssembler assembler, BusinessClock clock) {
        this.jobRepository = jobRepository;
        this.referenceValidator = referenceValidator;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Transactional
    public JobResponse execute(CreateJobCommand command, CurrentUser user) {
        JobDetails details = command.form().toDetails();
        referenceValidator.validate(details);

        Long recruiterId = user.userId();
        if (!user.hasRole(Roles.RECRUITER)) {
            if (command.recruiterId() == null) {
                throw new IllegalArgumentException("Assigned recruiter is required");
            }
            referenceValidator.validateRecruiter(command.recruiterId());
            recruiterId = command.recruiterId();
        }
        Job job = Job.createDraft(details, recruiterId, user.actor(), clock.today(), clock.now());
        return assembler.toResponse(jobRepository.save(job));
    }
}
