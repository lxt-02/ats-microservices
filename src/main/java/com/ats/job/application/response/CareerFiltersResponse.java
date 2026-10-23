package com.ats.job.application.response;

import java.util.List;

/** Values for the Career Site filter dropdowns, taken from the currently published jobs. */
public record CareerFiltersResponse(List<DepartmentRef> departments, List<String> locations,
                                    List<String> employmentTypes) {
}
