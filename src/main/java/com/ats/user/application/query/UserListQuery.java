package com.ats.user.application.query;

import com.ats.user.domain.enums.UserRole;
import com.ats.user.domain.enums.UserStatus;

/** Filters are optional; {@code keyword} matches full name or email. {@code page} is 1-based. */
public record UserListQuery(String keyword, UserRole role, UserStatus status, int page, int size) {
}
