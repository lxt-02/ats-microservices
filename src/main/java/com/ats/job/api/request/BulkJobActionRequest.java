package com.ats.job.api.request;

import com.ats.job.application.command.BulkJobAction;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkJobActionRequest(
        @NotNull(message = "Action is required") BulkJobAction action,
        @NotEmpty(message = "Select at least one job")
        @Size(max = 100, message = "At most 100 jobs per bulk action")
        List<@NotNull Long> jobIds
) {
}
