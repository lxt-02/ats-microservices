package com.ats.job.application.service;

import com.ats.job.domain.repository.SkillRepository;
import com.ats.job.domain.valueobject.JobDetails;
import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.shared.exception.ResourceNotFoundException;
import com.ats.shared.security.Roles;
import com.ats.user.application.contract.UserAccountContract;
import com.ats.user.application.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/** Checks the ids a requisition points to: department, skills, hiring manager and recruiter. */
@Component
public class JobReferenceValidator {

    private final DepartmentDirectory departmentDirectory;
    private final SkillRepository skillRepository;
    private final UserAccountContract userAccounts;

    public JobReferenceValidator(DepartmentDirectory departmentDirectory, SkillRepository skillRepository,
                                 UserAccountContract userAccounts) {
        this.departmentDirectory = departmentDirectory;
        this.skillRepository = skillRepository;
        this.userAccounts = userAccounts;
    }

    public void validate(JobDetails details) {
        if (details.departmentId() != null && !departmentDirectory.exists(details.departmentId())) {
            throw new ResourceNotFoundException("Department not found: " + details.departmentId());
        }
        if (!details.skillIds().isEmpty()) {
            Set<Long> unknown = new HashSet<>(details.skillIds());
            unknown.removeAll(skillRepository.findExistingIds(details.skillIds()));
            if (!unknown.isEmpty()) {
                throw new IllegalArgumentException("Unknown skill ids: " + unknown);
            }
        }
        if (details.hiringManagerId() != null) {
            requireActiveUserWithRole(details.hiringManagerId(), Roles.HIRING_MANAGER, "Hiring manager");
        }
    }

    public void validateRecruiter(Long recruiterId) {
        requireActiveUserWithRole(recruiterId, Roles.RECRUITER, "Assigned recruiter");
    }

    private void requireActiveUserWithRole(Long userId, String role, String label) {
        UserResponse user = userAccounts.findById(userId).orElse(null);
        if (user == null || !role.equals(user.role())
                || !UserAccountContract.ACTIVE_STATUS.equals(user.status())) {
            throw new IllegalArgumentException(label + " must be an active user with role " + role);
        }
    }
}
