package com.ats.shared.web;

import com.ats.shared.constants.PaginationConstants;

/**
 * Paging and sorting parameters shared by list requests (query string: page, size, sort).
 * {@code sort} is "key" or "key,asc|desc"; allowed keys are declared with {@code @AllowedSortKeys}.
 */
public interface BasePagination {

    Integer page();

    Integer size();

    String sort();

    default int pageOrDefault() {
        return page() == null || page() < 1 ? PaginationConstants.DEFAULT_PAGE : page();
    }

    default int sizeOrDefault(int defaultSize) {
        if (size() == null || size() < 1) {
            return defaultSize;
        }
        return Math.min(size(), PaginationConstants.MAX_SIZE);
    }

    default int sizeOrDefault() {
        return sizeOrDefault(PaginationConstants.DEFAULT_SIZE);
    }

    /** Sort key without direction, or null. */
    default String sortKey() {
        if (sort() == null || sort().isBlank()) {
            return null;
        }
        return sort().split(",")[0].trim();
    }

    default boolean sortAscending() {
        if (sort() == null || !sort().contains(",")) {
            return false;
        }
        return "asc".equalsIgnoreCase(sort().split(",", 2)[1].trim());
    }
}
