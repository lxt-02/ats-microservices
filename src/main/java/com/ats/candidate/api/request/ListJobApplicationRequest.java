package com.ats.candidate.api.request;

import com.ats.shared.web.BasePagination;
import jakarta.validation.constraints.NotNull;

/** {@code ?jobId=1&page=1&size=20}. */
public record ListJobApplicationRequest(
        @NotNull(message = "jobId is required") Long jobId,
        Integer page,
        Integer size,
        String sort
) implements BasePagination {
}
