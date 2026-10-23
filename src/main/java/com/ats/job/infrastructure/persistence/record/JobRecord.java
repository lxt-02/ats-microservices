package com.ats.job.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Row of table {@code jobs}. Required skills are rows of {@code job_skills}. */
@Getter
@Setter
@NoArgsConstructor
public class JobRecord {

    private Long id;
    private Long departmentId;
    private Long recruiterId;
    private Long hiringManagerId;
    private String title;
    private String description;
    private String employmentType;
    private String location;
    private Integer positionsCount;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private LocalDate deadline;
    private String priority;
    private boolean requiresApproval;
    private String status;
    private Long approvedBy;
    private Instant approvedAt;
    private String rejectionReason;
    private String utmSource;
    private String utmMedium;
    private Instant publishedAt;
    private Instant closedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;
}
