package com.ats.candidate.application.service;

import com.ats.candidate.application.event.CvUploadedEvent;
import com.ats.candidate.application.port.out.CvFileStorage;
import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.repository.CvRepository;
import com.ats.candidate.domain.valueobject.CvFile;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Stores a validated CV file, records it as PENDING and queues it for parsing. Must run inside a transaction:
 * if the transaction rolls back, the stored file is deleted again.
 */
@Service
public class CvIntakeService {

    private final CvFileStorage storage;
    private final CvRepository cvRepository;
    private final ApplicationEventPublisher events;
    private final CandidateClock clock;

    public CvIntakeService(CvFileStorage storage, CvRepository cvRepository, ApplicationEventPublisher events,
                           CandidateClock clock) {
        this.storage = storage;
        this.cvRepository = cvRepository;
        this.events = events;
        this.clock = clock;
    }

    public Cv accept(Long candidateId, CvFile file, byte[] content, String actor) {
        String key = storage.store(content, file.fileName());
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        storage.delete(key);
                    }
                }
            });
        }
        Cv cv = cvRepository.save(Cv.upload(candidateId, file, key, actor, clock.now()));
        events.publishEvent(new CvUploadedEvent(cv.getId()));
        return cv;
    }
}
