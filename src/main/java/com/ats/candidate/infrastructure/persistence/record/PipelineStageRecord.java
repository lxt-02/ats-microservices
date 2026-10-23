package com.ats.candidate.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code pipeline_stages}. */
@Getter
@Setter
@NoArgsConstructor
public class PipelineStageRecord {

    private Long id;
    private String statusCode;
    private String stageName;
    private int stageOrder;
    private String color;
    private Instant updatedAt;
    private String updatedBy;
}
