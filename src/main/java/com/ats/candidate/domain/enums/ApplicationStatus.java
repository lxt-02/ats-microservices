package com.ats.candidate.domain.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Pipeline states and the allowed forward moves (BRULE-12): no state can be skipped, backward moves only
 * through Reopen. Terminal states: HIRED, OFFER_DECLINED, TRANSFERRED, REJECTED.
 * <pre>
 * NEW -> CV_SCREENING -> CV_PASSED -> TECHNICAL_INTERVIEW -> TECH_PASSED -> HM_INTERVIEW -> OFFERED -> HIRED
 *                                                                                                  -> OFFER_DECLINED
 * any non-terminal state before OFFERED -> REJECTED;  NEW / CV_SCREENING -> TRANSFERRED (BRULE-04)
 * </pre>
 */
public enum ApplicationStatus {
    NEW,
    CV_SCREENING,
    CV_PASSED,
    TECHNICAL_INTERVIEW,
    TECH_PASSED,
    HM_INTERVIEW,
    OFFERED,
    HIRED,
    OFFER_DECLINED,
    TRANSFERRED,
    REJECTED;

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> NEXT = Map.of(
            NEW, EnumSet.of(CV_SCREENING, REJECTED, TRANSFERRED),
            CV_SCREENING, EnumSet.of(CV_PASSED, REJECTED, TRANSFERRED),
            CV_PASSED, EnumSet.of(TECHNICAL_INTERVIEW, REJECTED),
            TECHNICAL_INTERVIEW, EnumSet.of(TECH_PASSED, REJECTED),
            TECH_PASSED, EnumSet.of(HM_INTERVIEW, REJECTED),
            HM_INTERVIEW, EnumSet.of(OFFERED, REJECTED),
            OFFERED, EnumSet.of(HIRED, OFFER_DECLINED));

    public Set<ApplicationStatus> next() {
        Set<ApplicationStatus> next = NEXT.get(this);
        return next == null ? Set.of() : Set.copyOf(next);
    }

    public boolean canMoveTo(ApplicationStatus target) {
        return next().contains(target);
    }

    public boolean isTerminal() {
        return next().isEmpty();
    }

    /** Round 1 (CV screening): the only stages a candidate can be transferred from (BRULE-04). */
    public boolean isScreeningRound() {
        return this == NEW || this == CV_SCREENING;
    }
}
