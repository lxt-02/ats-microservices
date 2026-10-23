package com.ats.organization.application.query;

/** Lists the children of {@code parentId}; root departments when it is null. */
public record DepartmentListQuery(Long parentId) {
}
