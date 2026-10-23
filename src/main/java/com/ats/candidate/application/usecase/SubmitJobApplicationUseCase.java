package com.ats.candidate.application.usecase;

import com.ats.candidate.application.command.SubmitApplicationCommand;
import com.ats.candidate.application.event.ApplicationSubmittedEvent;
import com.ats.candidate.application.response.SubmittedApplicationResponse;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.application.service.CvIntakeService;
import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.enums.ApplicationSource;
import com.ats.candidate.domain.exception.CandidateRuleViolation;
import com.ats.candidate.domain.repository.CandidateRepository;
import com.ats.candidate.domain.repository.JobApplicationRepository;
import com.ats.candidate.domain.valueobject.CvFile;
import com.ats.candidate.domain.valueobject.UtmTracking;
import com.ats.job.application.contract.JobDirectory;
import com.ats.job.application.contract.JobInfo;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

/**
 * UC-01.2 Submit Job Application (public). Checks BRULE-10 (job open), BRULE-08 (CV file), BRULE-09 (one
 * application per email and job); reuses the candidate profile with the same email; stores the CV and queues it
 * for parsing; publishes {@link ApplicationSubmittedEvent} for the auto-reply email.
 */
@Service
public class SubmitJobApplicationUseCase {

    private static final String ACTOR = "career-site";

    private final JobDirectory jobDirectory;
    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository applicationRepository;
    private final CvIntakeService cvIntake;
    private final ApplicationEventPublisher events;
    private final CandidateClock clock;

    public SubmitJobApplicationUseCase(JobDirectory jobDirectory, CandidateRepository candidateRepository,
                                       JobApplicationRepository applicationRepository, CvIntakeService cvIntake,
                                       ApplicationEventPublisher events, CandidateClock clock) {
        this.jobDirectory = jobDirectory;
        this.candidateRepository = candidateRepository;
        this.applicationRepository = applicationRepository;
        this.cvIntake = cvIntake;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public SubmittedApplicationResponse execute(SubmitApplicationCommand command) {
        JobInfo job = jobDirectory.findById(command.jobId())
                .filter(found -> !"DRAFT".equals(found.status()) && !"PENDING_APPROVAL".equals(found.status()))
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + command.jobId()));
        if (!job.acceptingApplications()) {
            throw CandidateRuleViolation.jobNotAcceptingApplications(job.id());
        }
        if (command.cv() == null) {
            throw CandidateRuleViolation.cvRequired();
        }
        CvFile cvFile = CvFile.validate(command.cv().fileName(), command.cv().content());

        Instant now = clock.now();
        UtmTracking utm = new UtmTracking(command.utmSource(), command.utmMedium(), command.utmCampaign());
        String email = command.email().trim().toLowerCase(Locale.ROOT);
        Candidate candidate = candidateRepository.findByEmail(email).orElse(null);
        if (candidate != null) {
            if (applicationRepository.existsByJobIdAndCandidateId(job.id(), candidate.getId())) {
                throw CandidateRuleViolation.duplicateApplication();
            }
            candidate.refreshContact(command.fullName(), command.phone(), ACTOR, now);
            candidate = candidateRepository.save(candidate);
        } else {
            String source = command.source() == null ? null : command.source().name();
            candidate = candidateRepository.save(Candidate.registerApplicant(command.fullName(), email,
                    command.phone(), source, utm, ACTOR, now));
            Candidate created = candidate;
            candidateRepository.findOtherIdByEmailOrPhone(created.getId(), null, created.getPhone())
                    .ifPresent(otherId -> created.markDuplicateOf(otherId, ACTOR, now));
            candidate = candidateRepository.save(created);
        }

        Cv cv = cvIntake.accept(candidate.getId(), cvFile, command.cv().content(), ACTOR);
        JobApplication application;
        try {
            application = applicationRepository.save(JobApplication.submit(job.id(), candidate.getId(), cv.getId(),
                    command.coverLetter(), command.source() == null ? ApplicationSource.OTHER : command.source(),
                    command.referralCode(), utm, ACTOR, now));
        } catch (DuplicateKeyException ex) {
            // two submissions of the same form at the same time
            throw CandidateRuleViolation.duplicateApplication();
        }

        String code = JobApplication.code(application.getId(), application.getAppliedAt(), clock.zone());
        events.publishEvent(new ApplicationSubmittedEvent(application.getId(), code, job.id(), job.title(),
                candidate.getId(), candidate.getFullName(), candidate.getEmail(), application.getAppliedAt()));
        return new SubmittedApplicationResponse(application.getId(), code, job.id(), job.title(),
                application.getStatus().name(), application.getAppliedAt());
    }
}
