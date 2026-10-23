package com.ats.candidate.infrastructure.persistence.repository;

import com.ats.candidate.domain.aggregate.EvaluationNote;
import com.ats.candidate.domain.repository.EvaluationNoteRepository;
import com.ats.candidate.infrastructure.persistence.mapper.EvaluationNoteMyBatisMapper;
import com.ats.candidate.infrastructure.persistence.record.EvaluationNoteRecord;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MyBatisEvaluationNoteRepository implements EvaluationNoteRepository {

    private final EvaluationNoteMyBatisMapper mapper;

    public MyBatisEvaluationNoteRepository(EvaluationNoteMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public EvaluationNote save(EvaluationNote note) {
        EvaluationNoteRecord record = new EvaluationNoteRecord();
        record.setApplicationId(note.getApplicationId());
        record.setAuthorId(note.getAuthorId());
        record.setContent(note.getContent());
        record.setCreatedAt(note.getCreatedAt());
        record.setCreatedBy(String.valueOf(note.getAuthorId()));
        mapper.insert(record);
        return EvaluationNote.restore(record.getId(), note.getApplicationId(), note.getAuthorId(), note.getContent(),
                note.getCreatedAt());
    }

    @Override
    public List<EvaluationNote> findByApplicationId(Long applicationId) {
        return mapper.findActiveByApplicationId(applicationId).stream()
                .map(record -> EvaluationNote.restore(record.getId(), record.getApplicationId(),
                        record.getAuthorId(), record.getContent(), record.getCreatedAt()))
                .toList();
    }
}
