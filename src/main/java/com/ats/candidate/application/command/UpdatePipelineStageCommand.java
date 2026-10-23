package com.ats.candidate.application.command;

public record UpdatePipelineStageCommand(Long stageId, String name, int order, String color) {
}
