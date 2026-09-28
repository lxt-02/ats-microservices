package com.ats.userservice.domain.model.user.event;

import com.ats.userservice.domain.model.user.valueobject.UserId;

import java.time.Instant;

public record UserCreatedEvent(UserId userId, Instant occurredAt) {
}
