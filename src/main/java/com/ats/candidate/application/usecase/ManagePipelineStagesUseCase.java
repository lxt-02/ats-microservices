package com.ats.candidate.application.usecase;

import com.ats.candidate.application.command.UpdatePipelineStageCommand;
import com.ats.candidate.application.response.PipelineStageResponse;
import com.ats.candidate.application.service.CandidateClock;
import com.ats.candidate.domain.aggregate.PipelineStage;
import com.ats.candidate.domain.repository.PipelineStageRepository;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** UC-04.3 Configure Kanban Stages: list, and rename / reorder / recolour a column. */
@Service
public class ManagePipelineStagesUseCase {

    private final PipelineStageRepository stageRepository;
    private final CandidateClock clock;

    public ManagePipelineStagesUseCase(PipelineStageRepository stageRepository, CandidateClock clock) {
        this.stageRepository = stageRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PipelineStageResponse> list() {
        return stageRepository.findAll().stream().map(PipelineStageResponse::from).toList();
    }

    @Transactional
    public PipelineStageResponse update(UpdatePipelineStageCommand command, String actor) {
        PipelineStage stage = stageRepository.findById(command.stageId())
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline stage not found: " + command.stageId()));
        stage.configure(command.name(), command.order(), command.color(), actor, clock.now());
        return PipelineStageResponse.from(stageRepository.save(stage));
    }
}
