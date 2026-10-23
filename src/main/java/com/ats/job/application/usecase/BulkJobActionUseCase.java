package com.ats.job.application.usecase;

import com.ats.job.application.command.BulkJobAction;
import com.ats.job.application.response.BulkJobActionResponse;
import com.ats.job.application.response.BulkJobActionResponse.Result;
import com.ats.shared.exception.ApplicationException;
import com.ats.shared.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * "Bulk Actions" of the Job Management table. Each job is handled in its own transaction, so one failing job
 * does not block the others; the response reports the outcome per job.
 */
@Service
public class BulkJobActionUseCase {

    private final PublishJobUseCase publishJobUseCase;
    private final CloseJobUseCase closeJobUseCase;
    private final DeleteJobUseCase deleteJobUseCase;

    public BulkJobActionUseCase(PublishJobUseCase publishJobUseCase, CloseJobUseCase closeJobUseCase,
                                DeleteJobUseCase deleteJobUseCase) {
        this.publishJobUseCase = publishJobUseCase;
        this.closeJobUseCase = closeJobUseCase;
        this.deleteJobUseCase = deleteJobUseCase;
    }

    public BulkJobActionResponse execute(BulkJobAction action, List<Long> jobIds, CurrentUser user) {
        List<Result> results = new ArrayList<>();
        for (Long jobId : new LinkedHashSet<>(jobIds)) {
            try {
                switch (action) {
                    case PUBLISH -> publishJobUseCase.execute(jobId, user);
                    case CLOSE -> closeJobUseCase.execute(jobId, user);
                    case DELETE -> deleteJobUseCase.execute(jobId, user);
                }
                results.add(new Result(jobId, true, null));
            } catch (ApplicationException | AccessDeniedException | IllegalArgumentException ex) {
                results.add(new Result(jobId, false, ex.getMessage()));
            }
        }
        return BulkJobActionResponse.of(results);
    }
}
