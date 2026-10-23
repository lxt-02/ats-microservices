package com.ats.job.api.request;

import com.ats.job.application.query.CareerJobQuery;
import com.ats.job.application.query.CareerJobSort;
import com.ats.job.domain.enums.EmploymentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** {@code ?keyword=java&departmentId=1&location=Hanoi&employmentType=FULL_TIME&sort=MOST_RECENT&page=1&size=6}. */
public record ListCareerJobRequest(
        @Size(max = 200) String keyword,
        Long departmentId,
        @Size(max = 200) String location,
        EmploymentType employmentType,
        CareerJobSort sort,
        @Min(1) Integer page,
        @Min(1) @Max(50) Integer size
) {

    public static final int DEFAULT_SIZE = 6;

    public CareerJobQuery toQuery() {
        return new CareerJobQuery(keyword, departmentId, location, employmentType,
                sort == null ? CareerJobSort.MOST_RECENT : sort,
                page == null ? 1 : page, size == null ? DEFAULT_SIZE : size);
    }
}
