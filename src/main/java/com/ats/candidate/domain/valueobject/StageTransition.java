package com.ats.candidate.domain.valueobject;

import com.ats.candidate.domain.enums.ApplicationStatus;

import java.time.Instant;

/**
 * Audit record of a pipeline move (previous state, new state, who, when, note). {@code fromStatus} is null for
 * the creation of the application. {@code id} is null until saved.
 */
public record StageTransition(Long id, ApplicationStatus fromStatus, ApplicationStatus toStatus, String note,
                              String movedBy, Instant movedAt) {
}
