package com.ats.candidate.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Evaluation note, or the optional note of a Reopen. */
public record NoteRequest(
        @NotBlank(message = "This field is required.")
        @Size(max = 5000, message = "Note must not exceed 5,000 characters")
        String content
) {
}
