package com.ats.candidate.application.response;

/** MSG-PIPE-001. */
public record TransferResponse(Long sourceApplicationId, Long targetApplicationId, String targetApplicationCode,
                               Long targetJobId, String targetJobTitle, String message) {
}
