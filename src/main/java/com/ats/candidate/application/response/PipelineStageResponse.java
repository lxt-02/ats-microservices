package com.ats.candidate.application.response;

import com.ats.candidate.domain.aggregate.PipelineStage;

public record PipelineStageResponse(Long id, String status, String name, int order, String color) {

    public static PipelineStageResponse from(PipelineStage stage) {
        return new PipelineStageResponse(stage.getId(), stage.getStatus().name(), stage.getName(), stage.getOrder(),
                stage.getColor());
    }
}
