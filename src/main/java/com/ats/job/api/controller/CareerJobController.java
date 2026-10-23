package com.ats.job.api.controller;

import com.ats.job.api.request.ListCareerJobRequest;
import com.ats.job.application.response.CareerFiltersResponse;
import com.ats.job.application.response.CareerJobDetailResponse;
import com.ats.job.application.response.CareerJobItemResponse;
import com.ats.job.application.usecase.GetCareerFiltersUseCase;
import com.ats.job.application.usecase.GetPublishedJobUseCase;
import com.ats.job.application.usecase.ListPublishedJobsUseCase;
import com.ats.shared.web.ApiResponse;
import com.ats.shared.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public Career Site (no login): UC-01.1 View Job Listings. */
@RestController
@RequestMapping("/api/v1/public/jobs")
public class CareerJobController {

    private static final int OK = HttpStatus.OK.value();

    private final ListPublishedJobsUseCase listPublishedJobsUseCase;
    private final GetPublishedJobUseCase getPublishedJobUseCase;
    private final GetCareerFiltersUseCase getCareerFiltersUseCase;

    public CareerJobController(ListPublishedJobsUseCase listPublishedJobsUseCase,
                               GetPublishedJobUseCase getPublishedJobUseCase,
                               GetCareerFiltersUseCase getCareerFiltersUseCase) {
        this.listPublishedJobsUseCase = listPublishedJobsUseCase;
        this.getPublishedJobUseCase = getPublishedJobUseCase;
        this.getCareerFiltersUseCase = getCareerFiltersUseCase;
    }

    /** An empty page means "There are no open positions at this time." (MSG-EMPTY-001). */
    @GetMapping
    public ApiResponse<PageResponse<CareerJobItemResponse>> list(@Valid ListCareerJobRequest request) {
        return ApiResponse.success(OK, listPublishedJobsUseCase.execute(request.toQuery()));
    }

    @GetMapping("/filters")
    public ApiResponse<CareerFiltersResponse> filters() {
        return ApiResponse.success(OK, getCareerFiltersUseCase.execute());
    }

    @GetMapping("/{id}")
    public ApiResponse<CareerJobDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(OK, getPublishedJobUseCase.execute(id));
    }
}
