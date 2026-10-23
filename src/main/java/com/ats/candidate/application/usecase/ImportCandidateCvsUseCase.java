package com.ats.candidate.application.usecase;

import com.ats.candidate.application.command.ImportCvsCommand;
import com.ats.candidate.application.command.UploadedFile;
import com.ats.candidate.application.response.ImportCvsResponse;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.candidate.application.service.CvImportService;
import com.ats.job.application.contract.JobInfo;
import com.ats.shared.exception.ApplicationException;
import com.ats.shared.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * UC-02.3 Import Candidate CV (single or bulk). Every file is imported in its own transaction: a rejected file
 * (wrong format, too large) does not stop the others. Each accepted file creates a candidate profile that is
 * completed by the parser; with a job, it also creates an application.
 */
@Service
public class ImportCandidateCvsUseCase {

    public static final int MAX_FILES = 50;

    private final CvImportService cvImportService;
    private final CandidateAccessPolicy accessPolicy;

    public ImportCandidateCvsUseCase(CvImportService cvImportService, CandidateAccessPolicy accessPolicy) {
        this.cvImportService = cvImportService;
        this.accessPolicy = accessPolicy;
    }

    public ImportCvsResponse execute(ImportCvsCommand command, CurrentUser user) {
        if (command.files() == null || command.files().isEmpty()) {
            throw new IllegalArgumentException("Select at least one CV file");
        }
        if (command.files().size() > MAX_FILES) {
            throw new IllegalArgumentException("At most " + MAX_FILES + " files per import");
        }
        JobInfo job = command.jobId() == null ? null : accessPolicy.ensureCanAccessJob(command.jobId(), user);

        List<ImportCvsResponse.Result> results = new ArrayList<>();
        for (UploadedFile file : command.files()) {
            try {
                results.add(cvImportService.importOne(file, job, user.actor()));
            } catch (ApplicationException | IllegalArgumentException ex) {
                results.add(new ImportCvsResponse.Result(file.fileName(), false, null, null, null, ex.getMessage()));
            }
        }
        return ImportCvsResponse.of(results);
    }
}
