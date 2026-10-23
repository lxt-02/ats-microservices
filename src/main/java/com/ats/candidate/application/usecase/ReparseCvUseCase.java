package com.ats.candidate.application.usecase;

import com.ats.candidate.application.event.CvUploadedEvent;
import com.ats.candidate.application.response.CvResponse;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.application.service.CandidateLoader;
import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.repository.CvRepository;
import com.ats.shared.security.CurrentUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Queues a CV for another parsing attempt (e.g. after the skill master data was extended). */
@Service
public class ReparseCvUseCase {

    private final CandidateLoader candidateLoader;
    private final CandidateAccessPolicy accessPolicy;
    private final CvRepository cvRepository;
    private final ApplicationEventPublisher events;
    private final CandidateClock clock;

    public ReparseCvUseCase(CandidateLoader candidateLoader, CandidateAccessPolicy accessPolicy,
                            CvRepository cvRepository, ApplicationEventPublisher events, CandidateClock clock) {
        this.candidateLoader = candidateLoader;
        this.accessPolicy = accessPolicy;
        this.cvRepository = cvRepository;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public CvResponse execute(Long candidateId, Long cvId, CurrentUser user) {
        accessPolicy.ensureCanView(candidateId, user);
        Cv cv = candidateLoader.loadCv(candidateId, cvId);
        cv.requestReparse(user.actor(), clock.now());
        Cv saved = cvRepository.save(cv);
        events.publishEvent(new CvUploadedEvent(saved.getId()));
        return CvResponse.from(saved);
    }
}
