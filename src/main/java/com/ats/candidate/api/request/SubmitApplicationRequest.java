package com.ats.candidate.api.request;

import com.ats.candidate.application.command.SubmitApplicationCommand;
import com.ats.candidate.application.command.UploadedFile;
import com.ats.candidate.domain.enums.ApplicationSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

/** multipart/form-data of the Career Site application form (SCR-CS-002). Messages follow MSG-VAL-xxx. */
public record SubmitApplicationRequest(
        @NotBlank(message = "This field is required.")
        @Size(min = 2, max = 100, message = "Full name must be 2-100 characters")
        String fullName,
        @NotBlank(message = "Please enter a valid email address.")
        @Email(message = "Please enter a valid email address.")
        @Size(max = 255, message = "Please enter a valid email address.")
        String email,
        @Pattern(regexp = "^\\+?[0-9][0-9 .-]{7,18}[0-9]$", message = "Please enter a valid phone number.")
        String phone,
        @NotNull(message = "Please upload your CV (PDF or DOCX, max 5MB).")
        MultipartFile cv,
        @Size(max = 5000, message = "Cover letter must not exceed 5,000 characters")
        String coverLetter,
        ApplicationSource source,
        @Size(max = 20, message = "Referral code must not exceed 20 characters")
        String referralCode,
        @Size(max = 150) String utmSource,
        @Size(max = 150) String utmMedium,
        @Size(max = 255) String utmCampaign
) {

    public SubmitApplicationCommand toCommand(Long jobId) {
        return new SubmitApplicationCommand(jobId, fullName, email, phone == null || phone.isBlank() ? null : phone,
                toUploadedFile(cv), coverLetter, source == ApplicationSource.IMPORT ? null : source, referralCode,
                utmSource, utmMedium, utmCampaign);
    }

    static UploadedFile toUploadedFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return new UploadedFile(file.getOriginalFilename(), file.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read the uploaded file", ex);
        }
    }
}
