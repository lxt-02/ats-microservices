package com.ats.candidate.domain.repository;

import com.ats.candidate.domain.aggregate.EvaluationNote;

import java.util.List;

public interface EvaluationNoteRepository {

    EvaluationNote save(EvaluationNote note);

    /** Newest first. */
    List<EvaluationNote> findByApplicationId(Long applicationId);
}
