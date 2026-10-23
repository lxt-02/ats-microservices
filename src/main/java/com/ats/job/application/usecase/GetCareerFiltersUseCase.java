package com.ats.job.application.usecase;

import com.ats.job.application.port.out.JobReadQuery;
import com.ats.job.application.response.CareerFiltersResponse;
import com.ats.job.application.response.DepartmentRef;
import com.ats.job.application.service.BusinessClock;
import com.ats.job.domain.enums.EmploymentType;
import com.ats.organization.application.contract.DepartmentDirectory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class GetCareerFiltersUseCase {

    private final JobReadQuery jobReadQuery;
    private final DepartmentDirectory departmentDirectory;
    private final BusinessClock clock;

    public GetCareerFiltersUseCase(JobReadQuery jobReadQuery, DepartmentDirectory departmentDirectory,
                                   BusinessClock clock) {
        this.jobReadQuery = jobReadQuery;
        this.departmentDirectory = departmentDirectory;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CareerFiltersResponse execute() {
        LocalDate today = clock.today();
        Map<Long, String> names = departmentDirectory.findNames(jobReadQuery.findDepartmentIdsOfPublishedJobs(today));
        List<DepartmentRef> departments = names.entrySet().stream()
                .map(entry -> new DepartmentRef(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DepartmentRef::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new CareerFiltersResponse(departments, jobReadQuery.findLocationsOfPublishedJobs(today),
                Arrays.stream(EmploymentType.values()).map(Enum::name).toList());
    }
}
