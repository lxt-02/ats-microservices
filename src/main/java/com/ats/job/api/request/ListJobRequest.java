package com.ats.job.api.request;

import com.ats.job.application.query.JobListQuery;
import com.ats.job.domain.enums.JobStatus;
import com.ats.shared.validation.AllowedSortKeys;
import com.ats.shared.web.BasePagination;
import jakarta.validation.constraints.Size;

/** {@code ?keyword=&status=PUBLISHED&page=1&size=6&sort=createdAt,desc} (default page size 6). */
public record ListJobRequest(
        @Size(max = 200) String keyword,
        JobStatus status,
        Integer page,
        Integer size,
        @AllowedSortKeys({"createdAt", "title", "publishedAt", "deadline"}) String sort
) implements BasePagination {

    public static final int DEFAULT_SIZE = 6;

    public JobListQuery toQuery() {
        return new JobListQuery(keyword, status, sortKey(), sortAscending(), pageOrDefault(),
                sizeOrDefault(DEFAULT_SIZE));
    }
}
