package com.ats.organization.api.request;

import com.ats.organization.application.query.DepartmentListQuery;

/** Query string of GET /api/v1/departments. */
public record ListDepartmentRequest(Long parentId, Boolean all) {

    public DepartmentListQuery toQuery() {
        return new DepartmentListQuery(parentId, Boolean.TRUE.equals(all));
    }
}
