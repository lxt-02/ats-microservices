package com.ats.candidate.application.response;

import java.time.Instant;

public record HiringDecisionResponse(boolean approved, TeamRef team, String comment, Long decidedBy,
                                     Instant decidedAt) {
}
