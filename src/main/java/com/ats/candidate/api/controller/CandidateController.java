package com.ats.candidate.api.controller;

import com.ats.candidate.api.request.ImportCvsRequest;
import com.ats.candidate.api.request.SearchCandidateRequest;
import com.ats.candidate.api.request.UpdateCandidateProfileRequest;
import com.ats.candidate.application.response.CandidateListItemResponse;
import com.ats.candidate.application.response.CandidateProfileResponse;
import com.ats.candidate.application.response.CvDownload;
import com.ats.candidate.application.response.CvResponse;
import com.ats.candidate.application.response.ImportCvsResponse;
import com.ats.candidate.application.usecase.DownloadCvUseCase;
import com.ats.candidate.application.usecase.GetCandidateProfileUseCase;
import com.ats.candidate.application.usecase.ImportCandidateCvsUseCase;
import com.ats.candidate.application.usecase.ReparseCvUseCase;
import com.ats.candidate.application.usecase.SearchCandidatesUseCase;
import com.ats.candidate.application.usecase.UpdateCandidateProfileUseCase;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import com.ats.shared.security.Roles;
import com.ats.shared.web.ApiResponse;
import com.ats.shared.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Candidate Database (internal portal). Data isolation (BRULE-01) is applied in the use cases. */
@RestController
@RequestMapping("/api/v1/candidates")
@RequiresRole({Roles.ADMIN, Roles.RECRUITER})
public class CandidateController {

    private static final int OK = HttpStatus.OK.value();

    private final SearchCandidatesUseCase searchCandidatesUseCase;
    private final GetCandidateProfileUseCase getCandidateProfileUseCase;
    private final UpdateCandidateProfileUseCase updateCandidateProfileUseCase;
    private final ImportCandidateCvsUseCase importCandidateCvsUseCase;
    private final DownloadCvUseCase downloadCvUseCase;
    private final ReparseCvUseCase reparseCvUseCase;

    public CandidateController(SearchCandidatesUseCase searchCandidatesUseCase,
                               GetCandidateProfileUseCase getCandidateProfileUseCase,
                               UpdateCandidateProfileUseCase updateCandidateProfileUseCase,
                               ImportCandidateCvsUseCase importCandidateCvsUseCase,
                               DownloadCvUseCase downloadCvUseCase, ReparseCvUseCase reparseCvUseCase) {
        this.searchCandidatesUseCase = searchCandidatesUseCase;
        this.getCandidateProfileUseCase = getCandidateProfileUseCase;
        this.updateCandidateProfileUseCase = updateCandidateProfileUseCase;
        this.importCandidateCvsUseCase = importCandidateCvsUseCase;
        this.downloadCvUseCase = downloadCvUseCase;
        this.reparseCvUseCase = reparseCvUseCase;
    }

    /** UC-02.5. An empty page means "No matches found". */
    @GetMapping
    public ApiResponse<PageResponse<CandidateListItemResponse>> search(@Valid SearchCandidateRequest request,
                                                                       CurrentUser user) {
        return ApiResponse.success(OK, searchCandidatesUseCase.execute(request.toQuery(), user));
    }

    /** UC-02.6 (recruiter) / UC-03.2 (hiring manager). */
    @GetMapping("/{id}")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
    public ApiResponse<CandidateProfileResponse> get(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, getCandidateProfileUseCase.execute(id, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<CandidateProfileResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateCandidateProfileRequest request,
                                                        CurrentUser user) {
        return ApiResponse.success(OK, updateCandidateProfileUseCase.execute(request.toCommand(id), user));
    }

    /** UC-02.3: form fields {@code files} (one or more) and optional {@code jobId}. */
    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportCvsResponse> importCvs(@RequestPart("files") List<MultipartFile> files,
                                                    @RequestParam(required = false) Long jobId,
                                                    CurrentUser user) {
        return ApiResponse.success(OK, importCandidateCvsUseCase.execute(ImportCvsRequest.toCommand(files, jobId),
                user));
    }

    @GetMapping("/{id}/cvs/{cvId}/file")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
    public ResponseEntity<byte[]> download(@PathVariable Long id, @PathVariable Long cvId, CurrentUser user) {
        CvDownload file = downloadCvUseCase.execute(id, cvId, user);
        MediaType type = "PDF".equals(file.fileType()) ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.content());
    }

    @PostMapping("/{id}/cvs/{cvId}/parse")
    public ApiResponse<CvResponse> reparse(@PathVariable Long id, @PathVariable Long cvId, CurrentUser user) {
        return ApiResponse.success(OK, "CV queued for parsing.", reparseCvUseCase.execute(id, cvId, user));
    }
}
