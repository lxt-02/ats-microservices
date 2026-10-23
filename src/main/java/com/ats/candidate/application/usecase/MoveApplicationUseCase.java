package com.ats.candidate.application.usecase;

import com.ats.candidate.application.command.MoveApplicationCommand;
import com.ats.candidate.application.response.ApplicationDetailResponse;
import com.ats.candidate.application.service.ApplicationWorkflow;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-02.7 Move Candidate Stage (Kanban drag &amp; drop), UC-02.9 screening Pass/Reject, interview stages and
 * UC-02.18 offer outcome. The job's recruiter (or an admin) moves; BRULE-12 decides which moves exist.
 */
@Service
public class MoveApplicationUseCase {

    private final ApplicationWorkflow workflow;
    private final CandidateAccessPolicy accessPolicy;
    private final GetApplicationDetailUseCase detail;
    private final CandidateClock clock;

    public MoveApplicationUseCase(ApplicationWorkflow workflow, CandidateAccessPolicy accessPolicy,
                                  GetApplicationDetailUseCase detail, CandidateClock clock) {
        this.workflow = workflow;
        this.accessPolicy = accessPolicy;
        this.detail = detail;
        this.clock = clock;
    }

    @Transactional
    public ApplicationDetailResponse execute(MoveApplicationCommand command, CurrentUser user) {
        JobApplication application = workflow.load(command.applicationId());
        accessPolicy.ensureCanManageJob(application.getJobId(), user);
        application.moveTo(command.toStatus(), command.note(), user.actor(), clock.now());
        workflow.save(application);
        return detail.execute(application.getId(), user);
    }
}
