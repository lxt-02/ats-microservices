package com.ats.candidate.api.request;

import jakarta.validation.constraints.Size;

public record ReopenApplicationRequest(
        @Size(max = 2000, message = "Note must not exceed 2,000 characters") String note
) {
}
