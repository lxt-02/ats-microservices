package com.ats.job.infrastructure.persistence.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Row of a job list screen. */
@Getter
@Setter
@NoArgsConstructor
public class JobItemView {

    private Long id;
    private String title;
    private Long departmentId;
    private String employmentType;
    private String location;
    private String description;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String status;
    private String priority;
    private Long recruiterId;
    private Long hiringManagerId;
    private LocalDate deadline;
    private Instant publishedAt;
    private Instant createdAt;
}
