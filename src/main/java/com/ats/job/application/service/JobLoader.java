package com.ats.job.application.service;

import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.repository.JobRepository;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JobLoader {

    private final JobRepository jobRepository;

    public JobLoader(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job load(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }
}
