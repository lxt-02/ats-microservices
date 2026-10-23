package com.ats.auth.api.controller;

import com.ats.auth.api.request.ChangeSsoConfigurationStatusRequest;
import com.ats.auth.api.request.CreateSsoConfigurationRequest;
import com.ats.auth.api.request.UpdateSsoConfigurationRequest;
import com.ats.auth.application.response.SsoConfigurationResponse;
import com.ats.auth.application.usecase.ChangeSsoConfigurationStatusUseCase;
import com.ats.auth.application.usecase.CreateSsoConfigurationUseCase;
import com.ats.auth.application.usecase.DeleteSsoConfigurationUseCase;
import com.ats.auth.application.usecase.GetActiveSsoConfigurationUseCase;
import com.ats.auth.application.usecase.GetSsoConfigurationByIdUseCase;
import com.ats.auth.application.usecase.UpdateSsoConfigurationUseCase;
import com.ats.auth.domain.enums.SsoProviderType;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import com.ats.shared.security.Roles;
import com.ats.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sso-configurations")
@RequiresRole(Roles.ADMIN)
public class SsoConfigurationController {

    private static final int OK = HttpStatus.OK.value();

    private final CreateSsoConfigurationUseCase createUseCase;
    private final GetSsoConfigurationByIdUseCase getByIdUseCase;
    private final GetActiveSsoConfigurationUseCase getActiveUseCase;
    private final UpdateSsoConfigurationUseCase updateUseCase;
    private final ChangeSsoConfigurationStatusUseCase changeStatusUseCase;
    private final DeleteSsoConfigurationUseCase deleteUseCase;

    public SsoConfigurationController(CreateSsoConfigurationUseCase createUseCase,
                                      GetSsoConfigurationByIdUseCase getByIdUseCase,
                                      GetActiveSsoConfigurationUseCase getActiveUseCase,
                                      UpdateSsoConfigurationUseCase updateUseCase,
                                      ChangeSsoConfigurationStatusUseCase changeStatusUseCase,
                                      DeleteSsoConfigurationUseCase deleteUseCase) {
        this.createUseCase = createUseCase;
        this.getByIdUseCase = getByIdUseCase;
        this.getActiveUseCase = getActiveUseCase;
        this.updateUseCase = updateUseCase;
        this.changeStatusUseCase = changeStatusUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SsoConfigurationResponse>> create(
            @Valid @RequestBody CreateSsoConfigurationRequest request, CurrentUser currentUser) {
        SsoConfigurationResponse response = createUseCase.execute(request.toCommand(), currentUser.actor());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }

    @GetMapping("/{id}")
    public ApiResponse<SsoConfigurationResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(OK, getByIdUseCase.execute(id));
    }

    @GetMapping("/active/keycloak")
    public ApiResponse<SsoConfigurationResponse> getActiveKeycloak() {
        return ApiResponse.success(OK, getActiveUseCase.execute(SsoProviderType.KEYCLOAK));
    }

    @GetMapping("/active/google")
    public ApiResponse<SsoConfigurationResponse> getActiveGoogle() {
        return ApiResponse.success(OK, getActiveUseCase.execute(SsoProviderType.GOOGLE));
    }

    @PutMapping("/{id}")
    public ApiResponse<SsoConfigurationResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateSsoConfigurationRequest request,
                                                        CurrentUser currentUser) {
        return ApiResponse.success(OK, updateUseCase.execute(request.toCommand(id), currentUser.actor()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<SsoConfigurationResponse> changeStatus(@PathVariable Long id,
                                                              @Valid @RequestBody ChangeSsoConfigurationStatusRequest request,
                                                              CurrentUser currentUser) {
        return ApiResponse.success(OK, changeStatusUseCase.execute(request.toCommand(id), currentUser.actor()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, CurrentUser currentUser) {
        deleteUseCase.execute(id, currentUser.actor());
        return ResponseEntity.noContent().build();
    }
}
