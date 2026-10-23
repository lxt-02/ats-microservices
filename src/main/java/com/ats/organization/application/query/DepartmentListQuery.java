package com.ats.organization.application.query;

/**
 * {@code all = true}: every department (flat list for dropdowns).
 * Otherwise the children of {@code parentId}, or the root departments when it is null.
 */
public record DepartmentListQuery(Long parentId, boolean all) {
}
