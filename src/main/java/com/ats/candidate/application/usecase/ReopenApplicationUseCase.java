package com.ats.candidate.application.usecase;

import com.ats.candidate.application.response.ApplicationDetailResponse;
import com.ats.candidate.application.service.ApplicationWorkflow;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BRULE-12 explicit Reopen by the recruiter: back to the stage before the rejection / decline. */
@Service
public class ReopenApplicationUseCase {

    private final ApplicationWorkflow workflow;
    private final CandidateAccessPolicy accessPolicy;
    private final GetApplicationDetailUseCase detail;
    private final CandidateClock clock;

    public ReopenApplicationUseCase(ApplicationWorkflow workflow, CandidateAccessPolicy accessPolicy,
                                    GetApplicationDetailUseCase detail, CandidateClock clock) {
        this.workflow = workflow;
        this.accessPolicy = accessPolicy;
        this.detail = detail;
        this.clock = clock;
    }

    @Transactional
    public ApplicationDetailResponse execute(Long applicationId, String note, CurrentUser user) {
        JobApplication application = workflow.load(applicationId);
        accessPolicy.ensureCanManageJob(application.getJobId(), user);
        application.reopen(workflow.previousStatus(application), note, user.actor(), clock.now());
        workflow.save(application);
        return detail.execute(applicationId, user);
    }
}
