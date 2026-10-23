package com.ats.candidate.application.response;

import java.time.Instant;
import java.util.List;

/** UC-02.6 View Candidate Profile: parsed data, CV files and pipeline status per application. */
public record CandidateProfileResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String status,
        String source,
        String utmSource,
        String utmMedium,
        String utmCampaign,
        boolean duplicate,
        Long duplicateOfId,
        List<SkillRef> skills,
        List<CvResponse> cvs,
        List<CandidateApplicationResponse> applications,
        Instant createdAt,
        Instant updatedAt,
        String createdBy
) {
}
