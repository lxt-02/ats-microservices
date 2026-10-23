package com.ats.organization.application.contract;

import java.util.Collection;
import java.util.Map;

/** What other modules may ask about departments. */
public interface DepartmentDirectory {

    /** True when the department exists and is not deleted. */
    boolean exists(Long departmentId);

    /** Names of the existing (not deleted) departments among {@code departmentIds}, keyed by id. */
    Map<Long, String> findNames(Collection<Long> departmentIds);
}
