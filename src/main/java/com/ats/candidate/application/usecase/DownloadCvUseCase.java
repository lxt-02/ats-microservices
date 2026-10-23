package com.ats.candidate.application.usecase;

import com.ats.candidate.application.port.out.CvFileStorage;
import com.ats.candidate.application.response.CvDownload;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CandidateLoader;
import com.ats.candidate.domain.aggregate.Cv;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The original CV document. */
@Service
public class DownloadCvUseCase {

    private final CandidateLoader candidateLoader;
    private final CandidateAccessPolicy accessPolicy;
    private final CvFileStorage storage;

    public DownloadCvUseCase(CandidateLoader candidateLoader, CandidateAccessPolicy accessPolicy,
                             CvFileStorage storage) {
        this.candidateLoader = candidateLoader;
        this.accessPolicy = accessPolicy;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public CvDownload execute(Long candidateId, Long cvId, CurrentUser user) {
        accessPolicy.ensureCanView(candidateId, user);
        Cv cv = candidateLoader.loadCv(candidateId, cvId);
        return new CvDownload(cv.getFileName(), cv.getFileType().name(), storage.load(cv.getStorageKey()));
    }
}
