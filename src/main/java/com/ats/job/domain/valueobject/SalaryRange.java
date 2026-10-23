package com.ats.job.domain.valueobject;

import com.ats.job.domain.enums.SalaryCurrency;
import com.ats.job.domain.exception.JobRuleViolation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** BRULE-14: non-negative amounts with at most 2 decimals; max >= min when both are given. */
public record SalaryRange(BigDecimal min, BigDecimal max, SalaryCurrency currency) {

    public SalaryRange {
        min = normalize(min);
        max = normalize(max);
        currency = currency == null ? SalaryCurrency.VND : currency;
        if (min != null && max != null && max.compareTo(min) < 0) {
            throw JobRuleViolation.salaryRangeInvalid();
        }
    }

    public static SalaryRange none() {
        return new SalaryRange(null, null, SalaryCurrency.VND);
    }

    public boolean isDisclosed() {
        return min != null || max != null;
    }

    private static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Salary must not be negative");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Salary accepts at most 2 decimal places");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }
}
