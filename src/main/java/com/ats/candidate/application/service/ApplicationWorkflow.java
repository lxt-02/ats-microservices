package com.ats.candidate.application.service;

import com.ats.candidate.application.event.ApplicationStatusChangedEvent;
import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.enums.ApplicationStatus;
import com.ats.candidate.domain.repository.JobApplicationRepository;
import com.ats.candidate.domain.valueobject.StageTransition;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/** Loads applications, saves them with their audit trail and publishes the state changes. */
@Service
public class ApplicationWorkflow {

    private final JobApplicationRepository applicationRepository;
    private final ApplicationEventPublisher events;

    public ApplicationWorkflow(JobApplicationRepository applicationRepository, ApplicationEventPublisher events) {
        this.applicationRepository = applicationRepository;
        this.events = events;
    }

    public JobApplication load(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
    }

    /** The state before the current one (e.g. the stage a rejected application was rejected from). */
    public ApplicationStatus previousStatus(JobApplication application) {
        List<StageTransition> transitions = applicationRepository.findTransitions(application.getId());
        for (int i = transitions.size() - 1; i >= 0; i--) {
            StageTransition transition = transitions.get(i);
            if (transition.toStatus() == application.getStatus()) {
                return transition.fromStatus();
            }
        }
        return null;
    }

    public JobApplication save(JobApplication application) {
        List<StageTransition> transitions = application.getNewTransitions();
        JobApplication saved = applicationRepository.save(application);
        for (StageTransition transition : transitions) {
            if (transition.fromStatus() != null) {
                events.publishEvent(new ApplicationStatusChangedEvent(saved.getId(), saved.getJobId(),
                        saved.getCandidateId(), transition.fromStatus().name(), transition.toStatus().name(),
                        transition.note(), transition.movedBy(), transition.movedAt()));
            }
        }
        return saved;
    }
}
