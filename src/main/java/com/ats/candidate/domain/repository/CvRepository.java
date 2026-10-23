package com.ats.candidate.domain.repository;

import com.ats.candidate.domain.aggregate.Cv;

import java.util.List;
import java.util.Optional;

public interface CvRepository {

    Cv save(Cv cv);

    Optional<Cv> findById(Long id);

    /** Newest first. */
    List<Cv> findByCandidateId(Long candidateId);
}
