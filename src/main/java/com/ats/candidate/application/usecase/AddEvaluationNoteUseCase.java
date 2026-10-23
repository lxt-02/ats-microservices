package com.ats.candidate.application.usecase;

import com.ats.candidate.application.response.EvaluationNoteResponse;
import com.ats.candidate.application.service.ApplicationWorkflow;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.EvaluationNote;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.repository.EvaluationNoteRepository;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** UC-02.8 Add Evaluation Notes (recruiter or hiring manager of the job), and the list of notes. */
@Service
public class AddEvaluationNoteUseCase {

    private final ApplicationWorkflow workflow;
    private final CandidateAccessPolicy accessPolicy;
    private final EvaluationNoteRepository noteRepository;
    private final CandidateClock clock;

    public AddEvaluationNoteUseCase(ApplicationWorkflow workflow, CandidateAccessPolicy accessPolicy,
                                    EvaluationNoteRepository noteRepository, CandidateClock clock) {
        this.workflow = workflow;
        this.accessPolicy = accessPolicy;
        this.noteRepository = noteRepository;
        this.clock = clock;
    }

    @Transactional
    public EvaluationNoteResponse execute(Long applicationId, String content, CurrentUser user) {
        JobApplication application = workflow.load(applicationId);
        accessPolicy.ensureCanAccessJob(application.getJobId(), user);
        return EvaluationNoteResponse.from(noteRepository.save(
                EvaluationNote.write(applicationId, user.userId(), content, clock.now())));
    }

    @Transactional(readOnly = true)
    public List<EvaluationNoteResponse> list(Long applicationId, CurrentUser user) {
        JobApplication application = workflow.load(applicationId);
        accessPolicy.ensureCanAccessJob(application.getJobId(), user);
        return noteRepository.findByApplicationId(applicationId).stream().map(EvaluationNoteResponse::from).toList();
    }
}
