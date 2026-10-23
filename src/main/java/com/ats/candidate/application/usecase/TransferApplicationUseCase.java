package com.ats.candidate.application.usecase;

import com.ats.candidate.application.command.TransferApplicationCommand;
import com.ats.candidate.application.event.CandidateTransferredEvent;
import com.ats.candidate.application.response.TransferResponse;
import com.ats.candidate.application.service.ApplicationWorkflow;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.exception.CandidateRuleViolation;
import com.ats.candidate.domain.repository.JobApplicationRepository;
import com.ats.job.application.contract.JobDirectory;
import com.ats.job.application.contract.JobInfo;
import com.ats.shared.exception.ConflictException;
import com.ats.shared.security.CurrentUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * UC-02.10 Transfer Candidate to Another Job (BRULE-04): the source application becomes TRANSFERRED and a NEW
 * application is created on the target job, linked to its source ("Transferred from Job X by Recruiter Y").
 */
@Service
public class TransferApplicationUseCase {

    private final ApplicationWorkflow workflow;
    private final CandidateAccessPolicy accessPolicy;
    private final JobApplicationRepository applicationRepository;
    private final JobDirectory jobDirectory;
    private final ApplicationEventPublisher events;
    private final CandidateClock clock;

    public TransferApplicationUseCase(ApplicationWorkflow workflow, CandidateAccessPolicy accessPolicy,
                                      JobApplicationRepository applicationRepository, JobDirectory jobDirectory,
                                      ApplicationEventPublisher events, CandidateClock clock) {
        this.workflow = workflow;
        this.accessPolicy = accessPolicy;
        this.applicationRepository = applicationRepository;
        this.jobDirectory = jobDirectory;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public TransferResponse execute(TransferApplicationCommand command, CurrentUser user) {
        JobApplication source = workflow.load(command.applicationId());
        JobInfo sourceJob = accessPolicy.ensureCanManageJob(source.getJobId(), user);
        JobInfo target = jobDirectory.findById(command.targetJobId())
                .filter(JobInfo::acceptingApplications)
                .filter(job -> !job.id().equals(source.getJobId()))
                .orElseThrow(CandidateRuleViolation::invalidTransferTarget);
        if (applicationRepository.existsByJobIdAndCandidateId(target.id(), source.getCandidateId())) {
            throw new ConflictException("The candidate already has an application for the target job");
        }

        Instant now = clock.now();
        String note = command.note() == null || command.note().isBlank()
                ? "Transferred from job " + sourceJob.id() + " (" + sourceJob.title() + ") by user " + user.actor()
                : command.note().trim();
        source.markTransferred(workflow.previousStatus(source), "Transferred to job " + target.id(), user.actor(),
                now);
        workflow.save(source);

        JobApplication copy;
        try {
            copy = workflow.save(source.transferTo(target.id(), note, user.actor(), now));
        } catch (DuplicateKeyException ex) {
            throw new ConflictException("The candidate already has an application for the target job");
        }
        events.publishEvent(new CandidateTransferredEvent(source.getCandidateId(), source.getId(), sourceJob.id(),
                copy.getId(), target.id(), user.actor(), now));
        return new TransferResponse(source.getId(), copy.getId(),
                JobApplication.code(copy.getId(), copy.getAppliedAt(), clock.zone()), target.id(), target.title(),
                "Candidate has been transferred from " + sourceJob.title() + " to " + target.title() + ".");
    }
}
