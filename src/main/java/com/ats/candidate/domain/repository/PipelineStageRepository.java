package com.ats.candidate.domain.repository;

import com.ats.candidate.domain.aggregate.PipelineStage;

import java.util.List;
import java.util.Optional;

public interface PipelineStageRepository {

    /** Ordered by stage order. */
    List<PipelineStage> findAll();

    Optional<PipelineStage> findById(Long id);

    PipelineStage save(PipelineStage stage);
}
