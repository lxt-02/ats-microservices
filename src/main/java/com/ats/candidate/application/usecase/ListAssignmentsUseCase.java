package com.ats.candidate.application.usecase;

import com.ats.candidate.application.port.out.ApplicationReadQuery;
import com.ats.candidate.application.port.out.ApplicationReadQuery.AssignmentRow;
import com.ats.candidate.application.response.AssignmentResponse;
import com.ats.candidate.application.response.TeamRef;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.job.application.contract.JobDirectory;
import com.ats.job.application.contract.JobInfo;
import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * UC-02.16 View Candidate Assignment Status: candidates approved by a hiring manager with target team, approving
 * HM and date. Limited to the jobs the user is responsible for (admins see all, or one job with {@code jobId}).
 */
@Service
public class ListAssignmentsUseCase {

    private final ApplicationReadQuery readQuery;
    private final CandidateAccessPolicy accessPolicy;
    private final JobDirectory jobDirectory;
    private final DepartmentDirectory departmentDirectory;
    private final CandidateClock clock;

    public ListAssignmentsUseCase(ApplicationReadQuery readQuery, CandidateAccessPolicy accessPolicy,
                                  JobDirectory jobDirectory, DepartmentDirectory departmentDirectory,
                                  CandidateClock clock) {
        this.readQuery = readQuery;
        this.accessPolicy = accessPolicy;
        this.jobDirectory = jobDirectory;
        this.departmentDirectory = departmentDirectory;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> execute(Long jobId, CurrentUser user) {
        Collection<Long> jobIds;
        if (jobId != null) {
            accessPolicy.ensureCanAccessJob(jobId, user);
            jobIds = Set.of(jobId);
        } else if (user.hasRole(Roles.ADMIN)) {
            jobIds = null;
        } else {
            jobIds = jobDirectory.findJobIdsAssignedTo(user.userId());
            if (jobIds.isEmpty()) {
                return List.of();
            }
        }
        List<AssignmentRow> rows = readQuery.findApprovedAssignments(jobIds);
        Map<Long, JobInfo> jobs = jobDirectory.findByIds(rows.stream().map(AssignmentRow::jobId).toList());
        Map<Long, String> teams = departmentDirectory.findNames(
                rows.stream().map(AssignmentRow::assignedDepartmentId).filter(Objects::nonNull).toList());
        return rows.stream().map(row -> new AssignmentResponse(row.applicationId(),
                        JobApplication.code(row.applicationId(), row.appliedAt(), clock.zone()), row.jobId(),
                        jobs.containsKey(row.jobId()) ? jobs.get(row.jobId()).title() : null, row.candidateId(),
                        row.candidateName(), row.status(),
                        row.assignedDepartmentId() == null ? null
                                : new TeamRef(row.assignedDepartmentId(), teams.get(row.assignedDepartmentId())),
                        row.decidedBy(), row.decidedAt(), row.decisionComment()))
                .toList();
    }
}
