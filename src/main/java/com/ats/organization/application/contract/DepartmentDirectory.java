package com.ats.organization.application.contract;

/** What other modules may ask about departments. */
public interface DepartmentDirectory {

    /** True when the department exists and is not deleted. */
    boolean exists(Long departmentId);
}
