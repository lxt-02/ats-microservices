package com.ats.candidate.api.request;

import com.ats.candidate.application.command.UpdateCandidateProfileCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateCandidateProfileRequest(
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,
        @Email(message = "Please enter a valid email address.")
        @Size(max = 255)
        String email,
        @Pattern(regexp = "^\\+?[0-9][0-9 .-]{7,18}[0-9]$", message = "Please enter a valid phone number.")
        String phone,
        Set<Long> skillIds
) {

    public UpdateCandidateProfileCommand toCommand(Long candidateId) {
        return new UpdateCandidateProfileCommand(candidateId, fullName, email, phone, skillIds);
    }
}
