package com.ats.candidate.application.service;

import com.ats.candidate.application.event.CvUploadedEvent;
import com.ats.candidate.application.usecase.ParseCvUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** UC-02.4: parses each uploaded CV in the background once the upload is committed. */
@Component
public class CvParsingTrigger {

    private static final Logger log = LoggerFactory.getLogger(CvParsingTrigger.class);

    private final ParseCvUseCase parseCvUseCase;

    public CvParsingTrigger(ParseCvUseCase parseCvUseCase) {
        this.parseCvUseCase = parseCvUseCase;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCvUploaded(CvUploadedEvent event) {
        try {
            parseCvUseCase.execute(event.cvId());
        } catch (RuntimeException ex) {
            log.error("Parsing CV {} failed", event.cvId(), ex);
        }
    }
}
