package com.ats.candidate.api.request;

import com.ats.candidate.application.command.UpdatePipelineStageCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePipelineStageRequest(
        @NotBlank(message = "Stage name is required") @Size(max = 100) String name,
        @NotNull(message = "Order is required") @Min(1) @Max(100) Integer order,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Colour must look like #4f46e5") String color
) {

    public UpdatePipelineStageCommand toCommand(Long stageId) {
        return new UpdatePipelineStageCommand(stageId, name, order, color);
    }
}
