package com.ats.candidate.application.response;

import java.time.Instant;
import java.util.List;

public record CandidateListItemResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String source,
        boolean duplicate,
        List<String> skills,
        CvSummary latestCv,
        long applicationCount,
        Instant createdAt
) {
}
