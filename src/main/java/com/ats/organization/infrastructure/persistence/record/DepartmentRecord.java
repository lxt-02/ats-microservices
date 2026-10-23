package com.ats.organization.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code departments}. */
@Getter
@Setter
@NoArgsConstructor
public class DepartmentRecord {

    private Long id;
    private Long parentId;
    private String departmentName;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;
}
