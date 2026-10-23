package com.ats.job.api.request;

import com.ats.job.application.command.CreateSkillCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSkillRequest(
        @NotBlank(message = "Skill name is required")
        @Size(max = 150, message = "Skill name must not exceed 150 characters")
        String name,
        @Size(max = 150, message = "Category must not exceed 150 characters")
        String category
) {

    public CreateSkillCommand toCommand() {
        return new CreateSkillCommand(name, category);
    }
}
