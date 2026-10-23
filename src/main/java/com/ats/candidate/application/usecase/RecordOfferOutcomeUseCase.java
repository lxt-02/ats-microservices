package com.ats.candidate.application.usecase;

import com.ats.candidate.application.command.OfferOutcomeCommand;
import com.ats.candidate.application.response.ApplicationDetailResponse;
import com.ats.candidate.application.service.ApplicationWorkflow;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-02.18 Update Offer Status: Offered -> Hired (accepted) or Offer Declined. */
@Service
public class RecordOfferOutcomeUseCase {

    private final ApplicationWorkflow workflow;
    private final CandidateAccessPolicy accessPolicy;
    private final GetApplicationDetailUseCase detail;
    private final CandidateClock clock;

    public RecordOfferOutcomeUseCase(ApplicationWorkflow workflow, CandidateAccessPolicy accessPolicy,
                                     GetApplicationDetailUseCase detail, CandidateClock clock) {
        this.workflow = workflow;
        this.accessPolicy = accessPolicy;
        this.detail = detail;
        this.clock = clock;
    }

    @Transactional
    public ApplicationDetailResponse execute(OfferOutcomeCommand command, CurrentUser user) {
        JobApplication application = workflow.load(command.applicationId());
        accessPolicy.ensureCanManageJob(application.getJobId(), user);
        application.recordOfferOutcome(command.accepted(), command.note(), user.actor(), clock.now());
        workflow.save(application);
        return detail.execute(application.getId(), user);
    }
}
