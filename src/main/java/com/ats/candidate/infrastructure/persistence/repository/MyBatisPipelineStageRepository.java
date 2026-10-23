package com.ats.candidate.infrastructure.persistence.repository;

import com.ats.candidate.domain.aggregate.PipelineStage;
import com.ats.candidate.domain.enums.ApplicationStatus;
import com.ats.candidate.domain.repository.PipelineStageRepository;
import com.ats.candidate.infrastructure.persistence.mapper.PipelineStageMyBatisMapper;
import com.ats.candidate.infrastructure.persistence.record.PipelineStageRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisPipelineStageRepository implements PipelineStageRepository {

    private final PipelineStageMyBatisMapper mapper;

    public MyBatisPipelineStageRepository(PipelineStageMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<PipelineStage> findAll() {
        return mapper.findAll().stream().map(MyBatisPipelineStageRepository::toAggregate).toList();
    }

    @Override
    public Optional<PipelineStage> findById(Long id) {
        return mapper.findById(id).map(MyBatisPipelineStageRepository::toAggregate);
    }

    @Override
    public PipelineStage save(PipelineStage stage) {
        PipelineStageRecord record = new PipelineStageRecord();
        record.setId(stage.getId());
        record.setStatusCode(stage.getStatus().name());
        record.setStageName(stage.getName());
        record.setStageOrder(stage.getOrder());
        record.setColor(stage.getColor());
        record.setUpdatedAt(stage.getUpdatedAt());
        record.setUpdatedBy(stage.getUpdatedBy());
        mapper.update(record);
        return stage;
    }

    private static PipelineStage toAggregate(PipelineStageRecord record) {
        return PipelineStage.restore(record.getId(), ApplicationStatus.valueOf(record.getStatusCode()),
                record.getStageName(), record.getStageOrder(), record.getColor(), record.getUpdatedAt(),
                record.getUpdatedBy());
    }
}
