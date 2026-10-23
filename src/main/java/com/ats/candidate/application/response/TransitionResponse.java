package com.ats.candidate.application.response;

import com.ats.candidate.domain.valueobject.StageTransition;

import java.time.Instant;

/** One entry of the pipeline history. {@code movedBy} is the user id (or a system actor). */
public record TransitionResponse(String fromStatus, String toStatus, String note, String movedBy, Instant movedAt) {

    public static TransitionResponse from(StageTransition transition) {
        return new TransitionResponse(transition.fromStatus() == null ? null : transition.fromStatus().name(),
                transition.toStatus().name(), transition.note(), transition.movedBy(), transition.movedAt());
    }
}
