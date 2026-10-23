package com.ats.candidate.domain.repository;

import com.ats.candidate.domain.aggregate.Candidate;

import java.util.Optional;

/** Saves a candidate with its skills. Finders ignore soft-deleted candidates. */
public interface CandidateRepository {

    Candidate save(Candidate candidate);

    Optional<Candidate> findById(Long id);

    Optional<Candidate> findByEmail(String normalizedEmail);

    /** Oldest other candidate with the same email or phone, for duplicate detection. */
    Optional<Long> findOtherIdByEmailOrPhone(Long excludeId, String normalizedEmail, String phone);
}
