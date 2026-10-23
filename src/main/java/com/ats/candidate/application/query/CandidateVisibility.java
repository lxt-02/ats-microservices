package com.ats.candidate.application.query;

import java.util.Set;

/**
 * BRULE-01: which candidates a user may see. {@code all} for admins; otherwise candidates who applied to one of
 * {@code jobIds} (jobs assigned to the user) or, for recruiters, that the user imported ({@code createdBy}).
 */
public record CandidateVisibility(boolean all, String createdBy, Set<Long> jobIds) {

    public CandidateVisibility {
        jobIds = jobIds == null ? Set.of() : Set.copyOf(jobIds);
    }

    public static CandidateVisibility everything() {
        return new CandidateVisibility(true, null, Set.of());
    }
}
