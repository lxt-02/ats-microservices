package com.ats.candidate.application.command;

import com.ats.candidate.domain.enums.ApplicationSource;

/** UC-01.2 Career Site form. */
public record SubmitApplicationCommand(
        Long jobId,
        String fullName,
        String email,
        String phone,
        UploadedFile cv,
        String coverLetter,
        ApplicationSource source,
        String referralCode,
        String utmSource,
        String utmMedium,
        String utmCampaign
) {
}
