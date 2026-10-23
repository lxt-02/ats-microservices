package com.ats.candidate.domain;

import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.enums.ApplicationSource;
import com.ats.candidate.domain.enums.ApplicationStatus;
import com.ats.candidate.domain.valueobject.StageTransition;
import com.ats.shared.exception.RuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.ats.candidate.domain.enums.ApplicationStatus.CV_PASSED;
import static com.ats.candidate.domain.enums.ApplicationStatus.CV_SCREENING;
import static com.ats.candidate.domain.enums.ApplicationStatus.HIRED;
import static com.ats.candidate.domain.enums.ApplicationStatus.HM_INTERVIEW;
import static com.ats.candidate.domain.enums.ApplicationStatus.NEW;
import static com.ats.candidate.domain.enums.ApplicationStatus.OFFERED;
import static com.ats.candidate.domain.enums.ApplicationStatus.OFFER_DECLINED;
import static com.ats.candidate.domain.enums.ApplicationStatus.REJECTED;
import static com.ats.candidate.domain.enums.ApplicationStatus.TECHNICAL_INTERVIEW;
import static com.ats.candidate.domain.enums.ApplicationStatus.TECH_PASSED;
import static com.ats.candidate.domain.enums.ApplicationStatus.TRANSFERRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobApplicationTest {

    private static final Instant NOW = Instant.parse("2026-10-23T04:00:00Z");

    private static JobApplication fresh() {
        JobApplication application = JobApplication.submit(1L, 2L, 3L, null, ApplicationSource.LINKEDIN, null, null,
                "career-site", NOW);
        application.clearNewTransitions();
        return application;
    }

    private static JobApplication at(ApplicationStatus status) {
        return JobApplication.restore(10L, 1L, 2L, 3L, status, null, null, null, null, null, null, NOW, NOW, "x",
                NOW, "x");
    }

    private static String rule(Runnable action) {
        return assertThrows(RuleViolationException.class, action::run).getRule();
    }

    @Test
    void newApplicationRecordsItsFirstState() {
        JobApplication application = JobApplication.submit(1L, 2L, 3L, null, null, null, null, "career-site", NOW);
        assertEquals(List.of(new StageTransition(null, null, NEW, null, "career-site", NOW)),
                application.getNewTransitions());
    }

    @Test
    void stagesCannotBeSkippedAndEveryMoveIsAudited() {
        JobApplication application = fresh();
        assertEquals("APPLICATION_R3", rule(() -> application.moveTo(CV_PASSED, null, "5", NOW)));

        for (ApplicationStatus next : List.of(CV_SCREENING, CV_PASSED, TECHNICAL_INTERVIEW, TECH_PASSED,
                HM_INTERVIEW)) {
            application.moveTo(next, "ok", "5", NOW);
        }
        assertEquals(HM_INTERVIEW, application.getStatus());
        assertEquals(5, application.getNewTransitions().size());
        assertEquals(TECH_PASSED, application.getNewTransitions().get(4).fromStatus());
        // Offered needs the hiring manager's decision, transfer needs a target job
        assertEquals("APPLICATION_R3", rule(() -> application.moveTo(OFFERED, null, "5", NOW)));
        assertEquals("APPLICATION_R3", rule(() -> application.moveTo(TRANSFERRED, null, "5", NOW)));
    }

    @Test
    void rejectionFromAnyRoundAndReopenGoesBack() {
        JobApplication application = at(TECHNICAL_INTERVIEW);
        application.moveTo(REJECTED, "weak algorithms", "5", NOW);
        assertTrue(application.getStatus().isTerminal());
        assertEquals("APPLICATION_R3", rule(() -> application.moveTo(TECH_PASSED, null, "5", NOW)));

        assertEquals("APPLICATION_R7", rule(() -> at(CV_PASSED).reopen(CV_SCREENING, null, "5", NOW)));
        application.reopen(TECHNICAL_INTERVIEW, null, "5", NOW);
        assertEquals(TECHNICAL_INTERVIEW, application.getStatus());
    }

    @Test
    void hiringManagerDecision() {
        assertEquals("APPLICATION_R3", rule(() -> at(TECH_PASSED).decideAfterHmInterview(true, 4L, null, 7L, "7", NOW)));
        assertEquals("APPLICATION_R6", rule(() -> at(HM_INTERVIEW).decideAfterHmInterview(true, null, null, 7L, "7", NOW)));
        assertEquals("APPLICATION_R5", rule(() -> at(HM_INTERVIEW).decideAfterHmInterview(false, null, " ", 7L, "7", NOW)));

        JobApplication approved = at(HM_INTERVIEW);
        approved.decideAfterHmInterview(true, 4L, "Strong fit", 7L, "7", NOW);
        assertEquals(OFFERED, approved.getStatus());
        assertEquals(4L, approved.getHiringDecision().assignedDepartmentId());

        approved.recordOfferOutcome(false, null, "5", NOW);
        assertEquals(OFFER_DECLINED, approved.getStatus());
        approved.reopen(OFFERED, null, "5", NOW);
        assertNull(approved.getHiringDecision());
        approved.recordOfferOutcome(true, null, "5", NOW);
        assertEquals(HIRED, approved.getStatus());

        JobApplication rejected = at(HM_INTERVIEW);
        rejected.decideAfterHmInterview(false, 4L, "Not a culture fit", 7L, "7", NOW);
        assertEquals(REJECTED, rejected.getStatus());
        assertNull(rejected.getHiringDecision().assignedDepartmentId());
    }

    @Test
    void transferOnlyDuringRoundOne() {
        assertTrue(at(NEW).canTransfer(null));
        assertTrue(at(CV_SCREENING).canTransfer(NEW));
        assertTrue(at(REJECTED).canTransfer(CV_SCREENING));
        assertFalse(at(REJECTED).canTransfer(TECHNICAL_INTERVIEW));
        assertFalse(at(CV_PASSED).canTransfer(CV_SCREENING));
        assertEquals("APPLICATION_R4", rule(() -> at(CV_PASSED).markTransferred(CV_SCREENING, null, "5", NOW)));

        JobApplication source = at(CV_SCREENING);
        source.markTransferred(NEW, null, "5", NOW);
        JobApplication copy = source.transferTo(99L, null, "5", NOW);
        assertEquals(TRANSFERRED, source.getStatus());
        assertEquals(NEW, copy.getStatus());
        assertEquals(99L, copy.getJobId());
        assertEquals(10L, copy.getTransferredFromId());
        assertEquals("Transferred from job 1", copy.getNewTransitions().get(0).note());
    }
}
