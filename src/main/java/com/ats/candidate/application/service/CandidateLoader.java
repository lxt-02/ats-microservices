package com.ats.candidate.application.service;

import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.repository.CandidateRepository;
import com.ats.candidate.domain.repository.CvRepository;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CandidateLoader {

    private final CandidateRepository candidateRepository;
    private final CvRepository cvRepository;

    public CandidateLoader(CandidateRepository candidateRepository, CvRepository cvRepository) {
        this.candidateRepository = candidateRepository;
        this.cvRepository = cvRepository;
    }

    public Candidate load(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + id));
    }

    /** The CV must belong to the candidate. */
    public Cv loadCv(Long candidateId, Long cvId) {
        return cvRepository.findById(cvId)
                .filter(cv -> cv.getCandidateId().equals(candidateId))
                .orElseThrow(() -> new ResourceNotFoundException("CV not found: " + cvId));
    }
}
