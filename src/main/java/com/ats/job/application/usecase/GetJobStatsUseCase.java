package com.ats.job.application.usecase;

import com.ats.job.application.port.out.JobReadQuery;
import com.ats.job.application.response.JobStatsResponse;
import com.ats.job.application.service.JobAccessPolicy;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetJobStatsUseCase {

    private final JobReadQuery jobReadQuery;
    private final JobAccessPolicy accessPolicy;

    public GetJobStatsUseCase(JobReadQuery jobReadQuery, JobAccessPolicy accessPolicy) {
        this.jobReadQuery = jobReadQuery;
        this.accessPolicy = accessPolicy;
    }

    @Transactional(readOnly = true)
    public JobStatsResponse execute(CurrentUser user) {
        return jobReadQuery.countByStatus(accessPolicy.scopeFor(user));
    }
}
