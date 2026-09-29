package com.ats.userservice.api.rest;

import com.ats.userservice.api.dto.request.ChangeSsoStatusRequest;
import com.ats.userservice.api.dto.request.SsoConfigurationRequest;
import com.ats.userservice.api.dto.response.SsoConfigurationResponse;
import com.ats.userservice.api.mapper.SsoConfigurationResponseMapper;
import com.ats.userservice.application.command.sso.ChangeSsoStatusCommand;
import com.ats.userservice.application.command.sso.CreateSsoConfigurationCommand;
import com.ats.userservice.application.command.sso.UpdateSsoConfigurationCommand;
import com.ats.userservice.application.port.in.SsoConfigurationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sso-configurations")
public class SsoConfigurationController {

    private final SsoConfigurationUseCase ssoConfigurationUseCase;
    private final SsoConfigurationResponseMapper ssoConfigurationResponseMapper;

    public SsoConfigurationController(SsoConfigurationUseCase ssoConfigurationUseCase,
                                      SsoConfigurationResponseMapper ssoConfigurationResponseMapper) {
        this.ssoConfigurationUseCase = ssoConfigurationUseCase;
        this.ssoConfigurationResponseMapper = ssoConfigurationResponseMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SsoConfigurationResponse create(@Valid @RequestBody SsoConfigurationRequest request) {
        return ssoConfigurationResponseMapper.toResponse(ssoConfigurationUseCase.create(new CreateSsoConfigurationCommand(
                request.getProviderType(),
                request.getClientId(),
                request.getClientSecretRef(),
                request.getIssuerUri(),
                request.getRedirectUri(),
                request.getScopes(),
                request.getActor()
        )));
    }

    @GetMapping("/{id}")
    public SsoConfigurationResponse getById(@PathVariable Long id) {
        return ssoConfigurationResponseMapper.toResponse(ssoConfigurationUseCase.getById(id));
    }

    @GetMapping("/active/keycloak")
    public SsoConfigurationResponse getActiveKeycloak() {
        return ssoConfigurationResponseMapper.toResponse(ssoConfigurationUseCase.getActiveKeycloak());
    }

    @GetMapping("/active/google")
    public SsoConfigurationResponse getActiveGoogle() {
        return ssoConfigurationResponseMapper.toResponse(ssoConfigurationUseCase.getActiveGoogle());
    }

    @PutMapping("/{id}")
    public SsoConfigurationResponse update(@PathVariable Long id, @Valid @RequestBody SsoConfigurationRequest request) {
        return ssoConfigurationResponseMapper.toResponse(ssoConfigurationUseCase.update(new UpdateSsoConfigurationCommand(
                id,
                request.getClientId(),
                request.getClientSecretRef(),
                request.getIssuerUri(),
                request.getRedirectUri(),
                request.getScopes(),
                request.getActor()
        )));
    }

    @PatchMapping("/{id}/status")
    public SsoConfigurationResponse changeStatus(@PathVariable Long id,
                                                 @Valid @RequestBody ChangeSsoStatusRequest request) {
        return ssoConfigurationResponseMapper.toResponse(ssoConfigurationUseCase.changeStatus(new ChangeSsoStatusCommand(
                id,
                request.isActive(),
                request.getActor()
        )));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam(required = false) String actor) {
        ssoConfigurationUseCase.delete(id, actor);
    }
}
