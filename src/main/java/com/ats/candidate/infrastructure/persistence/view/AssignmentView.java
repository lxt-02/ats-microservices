package com.ats.candidate.infrastructure.persistence.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentView {

    private Long applicationId;
    private Long jobId;
    private Long candidateId;
    private String candidateName;
    private String status;
    private Instant appliedAt;
    private Long assignedDepartmentId;
    private Long decidedBy;
    private Instant decidedAt;
    private String decisionComment;
}
