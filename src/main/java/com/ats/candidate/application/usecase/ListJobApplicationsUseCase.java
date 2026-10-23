package com.ats.candidate.application.usecase;

import com.ats.candidate.application.port.out.CandidateReadQuery;
import com.ats.candidate.application.response.JobApplicationItemResponse;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.web.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applications received for one job (the job's recruiter, hiring manager, or an admin). */
@Service
public class ListJobApplicationsUseCase {

    private final CandidateReadQuery candidateReadQuery;
    private final CandidateAccessPolicy accessPolicy;
    private final CandidateClock clock;

    public ListJobApplicationsUseCase(CandidateReadQuery candidateReadQuery, CandidateAccessPolicy accessPolicy,
                                      CandidateClock clock) {
        this.candidateReadQuery = candidateReadQuery;
        this.accessPolicy = accessPolicy;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<JobApplicationItemResponse> execute(Long jobId, int page, int size, CurrentUser user) {
        accessPolicy.ensureCanAccessJob(jobId, user);
        return candidateReadQuery.findApplicationsByJobId(jobId, page, size).map(row ->
                new JobApplicationItemResponse(row.id(), JobApplication.code(row.id(), row.appliedAt(), clock.zone()),
                        row.candidateId(), row.candidateName(), row.candidateEmail(), row.candidatePhone(),
                        row.status(), row.source(), row.appliedAt(), row.cvId(), row.cvParseStatus()));
    }
}
