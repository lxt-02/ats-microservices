package com.ats.candidate.application.usecase;

import com.ats.candidate.application.response.ApplicationDetailResponse;
import com.ats.candidate.application.response.EvaluationNoteResponse;
import com.ats.candidate.application.response.HiringDecisionResponse;
import com.ats.candidate.application.response.TeamRef;
import com.ats.candidate.application.response.TransitionResponse;
import com.ats.candidate.application.service.ApplicationWorkflow;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.enums.ApplicationStatus;
import com.ats.candidate.domain.repository.CandidateRepository;
import com.ats.candidate.domain.repository.EvaluationNoteRepository;
import com.ats.candidate.domain.repository.JobApplicationRepository;
import com.ats.candidate.domain.repository.PipelineStageRepository;
import com.ats.candidate.domain.valueobject.HiringDecision;
import com.ats.job.application.contract.JobInfo;
import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** One application: candidate, current stage, history, notes and the actions the user may take. */
@Service
public class GetApplicationDetailUseCase {

    private final ApplicationWorkflow workflow;
    private final CandidateAccessPolicy accessPolicy;
    private final JobApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final EvaluationNoteRepository noteRepository;
    private final PipelineStageRepository stageRepository;
    private final DepartmentDirectory departmentDirectory;
    private final CandidateClock clock;

    public GetApplicationDetailUseCase(ApplicationWorkflow workflow, CandidateAccessPolicy accessPolicy,
                                       JobApplicationRepository applicationRepository,
                                       CandidateRepository candidateRepository,
                                       EvaluationNoteRepository noteRepository,
                                       PipelineStageRepository stageRepository,
                                       DepartmentDirectory departmentDirectory, CandidateClock clock) {
        this.workflow = workflow;
        this.accessPolicy = accessPolicy;
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.noteRepository = noteRepository;
        this.stageRepository = stageRepository;
        this.departmentDirectory = departmentDirectory;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponse execute(Long applicationId, CurrentUser user) {
        JobApplication application = workflow.load(applicationId);
        JobInfo job = accessPolicy.ensureCanAccessJob(application.getJobId(), user);
        Candidate candidate = candidateRepository.findById(application.getCandidateId()).orElse(null);
        ApplicationStatus status = application.getStatus();

        boolean manager = accessPolicy.canManageJob(job, user);
        List<String> moves = manager
                ? status.next().stream()
                .filter(next -> next != ApplicationStatus.OFFERED && next != ApplicationStatus.TRANSFERRED)
                .map(Enum::name).sorted().toList()
                : List.of();
        ApplicationStatus previous = workflow.previousStatus(application);
        boolean canTransfer = manager && application.canTransfer(previous);
        boolean canReopen = manager && (status == ApplicationStatus.REJECTED
                || status == ApplicationStatus.OFFER_DECLINED) && previous != null && !previous.isTerminal();
        boolean canDecide = status == ApplicationStatus.HM_INTERVIEW && accessPolicy.isHiringManagerOf(job, user);

        String stageName = stageRepository.findAll().stream()
                .filter(stage -> stage.getStatus() == status)
                .map(stage -> stage.getName())
                .findFirst().orElse(status.name());

        return new ApplicationDetailResponse(application.getId(),
                JobApplication.code(application.getId(), application.getAppliedAt(), clock.zone()),
                job.id(), job.title(), application.getCandidateId(),
                candidate == null ? null : candidate.getFullName(),
                candidate == null ? null : candidate.getEmail(),
                candidate == null ? null : candidate.getPhone(),
                application.getCvId(), status.name(), stageName, application.getCoverLetter(),
                application.getSource() == null ? null : application.getSource().name(),
                application.getReferralCode(), application.getUtm().source(), application.getUtm().medium(),
                application.getUtm().campaign(), application.getAppliedAt(), application.getStatusChangedAt(),
                application.getTransferredFromId(), decision(application.getHiringDecision()), moves, canTransfer,
                canReopen, canDecide,
                applicationRepository.findTransitions(applicationId).stream().map(TransitionResponse::from).toList(),
                noteRepository.findByApplicationId(applicationId).stream().map(EvaluationNoteResponse::from).toList());
    }

    private HiringDecisionResponse decision(HiringDecision decision) {
        if (decision == null) {
            return null;
        }
        TeamRef team = decision.assignedDepartmentId() == null ? null
                : new TeamRef(decision.assignedDepartmentId(), departmentDirectory
                .findNames(Set.of(decision.assignedDepartmentId())).get(decision.assignedDepartmentId()));
        return new HiringDecisionResponse(decision.approved(), team, decision.comment(), decision.decidedBy(),
                decision.decidedAt());
    }
}
