package com.ats.candidate.application.usecase;

import com.ats.candidate.application.command.HmDecisionCommand;
import com.ats.candidate.application.response.ApplicationDetailResponse;
import com.ats.candidate.application.service.ApplicationWorkflow;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.shared.exception.ResourceNotFoundException;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-03.3 Approve &amp; Assign (state Offered, target team recorded) / UC-03.4 Reject (state Rejected, reason).
 * Only the hiring manager of the job (BRULE-05); a rejection only concerns this job.
 */
@Service
public class DecideAfterHmInterviewUseCase {

    private final ApplicationWorkflow workflow;
    private final CandidateAccessPolicy accessPolicy;
    private final DepartmentDirectory departmentDirectory;
    private final GetApplicationDetailUseCase detail;
    private final CandidateClock clock;

    public DecideAfterHmInterviewUseCase(ApplicationWorkflow workflow, CandidateAccessPolicy accessPolicy,
                                         DepartmentDirectory departmentDirectory, GetApplicationDetailUseCase detail,
                                         CandidateClock clock) {
        this.workflow = workflow;
        this.accessPolicy = accessPolicy;
        this.departmentDirectory = departmentDirectory;
        this.detail = detail;
        this.clock = clock;
    }

    @Transactional
    public ApplicationDetailResponse execute(HmDecisionCommand command, CurrentUser user) {
        JobApplication application = workflow.load(command.applicationId());
        accessPolicy.ensureIsHiringManagerOf(application.getJobId(), user);
        if (command.approve() && command.teamId() != null && !departmentDirectory.exists(command.teamId())) {
            throw new ResourceNotFoundException("Team/unit not found: " + command.teamId());
        }
        application.decideAfterHmInterview(command.approve(), command.teamId(), command.comment(), user.userId(),
                user.actor(), clock.now());
        workflow.save(application);
        return detail.execute(application.getId(), user);
    }
}
