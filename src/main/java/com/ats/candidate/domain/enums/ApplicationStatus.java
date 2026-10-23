package com.ats.candidate.domain.enums;

/**
 * Pipeline states (BRULE-12). Applications start as NEW; moving between states belongs to the pipeline
 * (Kanban) feature.
 */
public enum ApplicationStatus {
    NEW,
    CV_SCREENING,
    CV_PASSED,
    TECHNICAL_INTERVIEW,
    TECH_PASSED,
    HM_INTERVIEW,
    HM_PASSED,
    OFFERED,
    HIRED,
    OFFER_DECLINED,
    TRANSFERRED,
    REJECTED
}
