package com.ats.candidate.application.service;

import com.ats.candidate.application.command.UploadedFile;
import com.ats.candidate.application.event.ApplicationSubmittedEvent;
import com.ats.candidate.application.response.ImportCvsResponse;
import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.enums.ApplicationSource;
import com.ats.candidate.domain.repository.CandidateRepository;
import com.ats.candidate.domain.repository.JobApplicationRepository;
import com.ats.candidate.domain.valueobject.CvFile;
import com.ats.candidate.domain.valueobject.UtmTracking;
import com.ats.job.application.contract.JobInfo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** One file of a recruiter import, in its own transaction. */
@Service
public class CvImportService {

    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository applicationRepository;
    private final CvIntakeService cvIntake;
    private final ApplicationEventPublisher events;
    private final CandidateClock clock;

    public CvImportService(CandidateRepository candidateRepository, JobApplicationRepository applicationRepository,
                           CvIntakeService cvIntake, ApplicationEventPublisher events, CandidateClock clock) {
        this.candidateRepository = candidateRepository;
        this.applicationRepository = applicationRepository;
        this.cvIntake = cvIntake;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public ImportCvsResponse.Result importOne(UploadedFile file, JobInfo job, String actor) {
        CvFile cvFile = CvFile.validate(file.fileName(), file.content());
        Instant now = clock.now();
        Candidate candidate = candidateRepository.save(Candidate.fromImportedCv(actor, now));
        Cv cv = cvIntake.accept(candidate.getId(), cvFile, file.content(), actor);

        Long applicationId = null;
        if (job != null) {
            JobApplication application = applicationRepository.save(JobApplication.submit(job.id(),
                    candidate.getId(), cv.getId(), null, ApplicationSource.IMPORT, null, UtmTracking.none(), actor,
                    now));
            applicationId = application.getId();
            events.publishEvent(new ApplicationSubmittedEvent(application.getId(),
                    JobApplication.code(application.getId(), application.getAppliedAt(), clock.zone()), job.id(),
                    job.title(), candidate.getId(), null, null, application.getAppliedAt()));
        }
        return new ImportCvsResponse.Result(cvFile.fileName(), true, candidate.getId(), cv.getId(), applicationId,
                "CV uploaded and queued for parsing.");
    }
}
