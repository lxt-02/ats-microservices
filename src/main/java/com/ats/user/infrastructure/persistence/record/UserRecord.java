package com.ats.user.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code users}. */
@Getter
@Setter
@NoArgsConstructor
public class UserRecord {

    private Long id;
    private Long departmentId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String role;
    private String authProvider;
    private String externalSubjectId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;
}
