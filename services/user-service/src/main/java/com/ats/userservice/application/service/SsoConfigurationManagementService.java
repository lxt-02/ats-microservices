package com.ats.userservice.application.service;

import com.ats.userservice.application.command.sso.ChangeSsoStatusCommand;
import com.ats.userservice.application.command.sso.CreateSsoConfigurationCommand;
import com.ats.userservice.application.command.sso.UpdateSsoConfigurationCommand;
import com.ats.userservice.application.exception.ResourceNotFoundException;
import com.ats.userservice.application.port.in.SsoConfigurationUseCase;
import com.ats.userservice.domain.model.sso.aggregate.SsoConfiguration;
import com.ats.userservice.domain.model.sso.enums.SsoProviderType;
import com.ats.userservice.domain.repository.SsoConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SsoConfigurationManagementService implements SsoConfigurationUseCase {

    private final SsoConfigurationRepository ssoConfigurationRepository;

    public SsoConfigurationManagementService(SsoConfigurationRepository ssoConfigurationRepository) {
        this.ssoConfigurationRepository = ssoConfigurationRepository;
    }

    @Override
    public SsoConfiguration create(CreateSsoConfigurationCommand command) {
        SsoProviderType providerType = command.providerType() == null ? SsoProviderType.GOOGLE : command.providerType();
        if (providerType == SsoProviderType.KEYCLOAK) {
            SsoConfiguration configuration = SsoConfiguration.createKeycloak(command.clientId(),
                    command.clientSecretRef(), command.issuerUri(), command.redirectUri(), command.scopes(),
                    command.createdBy());
            return ssoConfigurationRepository.save(configuration);
        }

        if (providerType != SsoProviderType.GOOGLE) {
            throw new IllegalArgumentException("Unsupported SSO provider: " + providerType);
        }

        SsoConfiguration configuration = SsoConfiguration.createGoogle(command.clientId(),
                command.clientSecretRef(), command.issuerUri(), command.redirectUri(), command.scopes(),
                command.createdBy());
        return ssoConfigurationRepository.save(configuration);
    }

    @Override
    @Transactional(readOnly = true)
    public SsoConfiguration getById(Long id) {
        return findConfiguration(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SsoConfiguration getActiveKeycloak() {
        return ssoConfigurationRepository.findActiveByProviderType(SsoProviderType.KEYCLOAK)
                .orElseThrow(() -> new ResourceNotFoundException("Active KEYCLOAK SSO configuration not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public SsoConfiguration getActiveGoogle() {
        return ssoConfigurationRepository.findActiveByProviderType(SsoProviderType.GOOGLE)
                .orElseThrow(() -> new ResourceNotFoundException("Active GOOGLE SSO configuration not found"));
    }

    @Override
    public SsoConfiguration update(UpdateSsoConfigurationCommand command) {
        SsoConfiguration configuration = findConfiguration(command.id());
        configuration.updateClient(command.clientId(), command.clientSecretRef(), command.updatedBy());
        configuration.updateIssuer(command.issuerUri(), command.updatedBy());
        configuration.updateRedirectAndScopes(command.redirectUri(), command.scopes(), command.updatedBy());
        return ssoConfigurationRepository.save(configuration);
    }

    @Override
    public SsoConfiguration changeStatus(ChangeSsoStatusCommand command) {
        SsoConfiguration configuration = findConfiguration(command.id());
        if (command.active()) {
            configuration.activate(command.updatedBy());
        } else {
            configuration.deactivate(command.updatedBy());
        }
        return ssoConfigurationRepository.save(configuration);
    }

    @Override
    public void delete(Long id, String updatedBy) {
        SsoConfiguration configuration = findConfiguration(id);
        configuration.markDeleted(updatedBy);
        ssoConfigurationRepository.save(configuration);
    }

    private SsoConfiguration findConfiguration(Long id) {
        return ssoConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SSO configuration not found: " + id));
    }
}
