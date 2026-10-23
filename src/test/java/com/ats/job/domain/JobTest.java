package com.ats.job.domain;

import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.enums.EmploymentType;
import com.ats.job.domain.enums.JobStatus;
import com.ats.job.domain.enums.SalaryCurrency;
import com.ats.job.domain.valueobject.JobDetails;
import com.ats.job.domain.valueobject.SalaryRange;
import com.ats.shared.exception.RuleViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 23);
    private static final Instant NOW = Instant.parse("2026-10-23T03:00:00Z");

    private static JobDetails details(String title, boolean requiresApproval, LocalDate deadline) {
        return new JobDetails(title, 1L, EmploymentType.FULL_TIME, "Hanoi", 2,
                new SalaryRange(new BigDecimal("1000"), new BigDecimal("2000"), SalaryCurrency.USD),
                "<p>Build things</p>", Set.of(1L, 2L), 7L, deadline, null, requiresApproval, null, null);
    }

    private static JobDetails incomplete() {
        return new JobDetails("Draft only", null, null, null, 1, null, null, null, null, null, null, true, null,
                null);
    }

    private static String rule(Runnable action) {
        return assertThrows(RuleViolationException.class, action::run).getRule();
    }

    @Test
    void draftNeedsOnlyATitleButSubmitNeedsEveryRequiredField() {
        assertEquals("JOB_R1", rule(() -> Job.createDraft(incomplete().withTitle(null), 5L, "5", TODAY, NOW)));

        Job job = Job.createDraft(incomplete(), 5L, "5", TODAY, NOW);
        RuleViolationException ex = assertThrows(RuleViolationException.class, () -> job.submit("5", TODAY, NOW));
        assertEquals("JOB_R1", ex.getRule());
        assertEquals(List.of("departmentId", "description", "skillIds", "hiringManagerId"), ex.getDetails().get("fields"));
    }

    @Test
    void approvalFlowBeforePublishing() {
        Job job = Job.createDraft(details("Java Dev", true, null), 5L, "5", TODAY, NOW);
        assertEquals("JOB_R5", rule(() -> job.publish("5", TODAY, NOW)));

        job.submit("5", TODAY, NOW);
        assertEquals(JobStatus.PENDING_APPROVAL, job.getStatus());
        assertEquals("JOB_R5", rule(() -> job.publish("5", TODAY, NOW)));

        job.reject("Salary too high", "7", NOW);
        assertEquals(JobStatus.DRAFT, job.getStatus());
        assertEquals("Salary too high", job.getRejectionReason());

        job.submit("5", TODAY, NOW);
        job.approve(7L, "7", NOW);
        assertTrue(job.isReadyToPublish(TODAY));
        job.publish("5", TODAY, NOW);
        assertEquals(JobStatus.PUBLISHED, job.getStatus());
        assertNotNull(job.getPublishedAt());
    }

    @Test
    void jobWithoutApprovalCanBePublishedDirectly() {
        Job job = Job.createDraft(details("Java Dev", false, null), 5L, "5", TODAY, NOW);
        job.submit("5", TODAY, NOW);
        assertEquals(JobStatus.DRAFT, job.getStatus());
        job.publish("5", TODAY, NOW);
        assertEquals(JobStatus.PUBLISHED, job.getStatus());
    }

    @Test
    void criticalFieldsAreLockedWhilePublished() {
        Job job = Job.createDraft(details("Java Dev", false, null), 5L, "5", TODAY, NOW);
        job.publish("5", TODAY, NOW);

        assertEquals("JOB_R4", rule(() -> job.revise(details("Renamed", false, null), "5", TODAY, NOW)));
        JobDetails newSalary = new JobDetails("Java Dev", 1L, EmploymentType.FULL_TIME, "Hanoi", 3, null,
                "<p>Updated</p>", Set.of(1L), 7L, null, null, false, null, null);
        job.revise(newSalary, "5", TODAY, NOW);
        assertEquals(3, job.getDetails().positionsCount());

        job.unpublish("5", NOW);
        job.revise(details("Renamed", false, null), "5", TODAY, NOW);
        assertEquals("Renamed", job.getTitle());
    }

    @Test
    void changingCriticalFieldsWithdrawsTheApproval() {
        Job job = Job.createDraft(details("Java Dev", true, null), 5L, "5", TODAY, NOW);
        job.submit("5", TODAY, NOW);
        job.approve(7L, "7", NOW);
        job.revise(details("Kotlin Dev", true, null), "5", TODAY, NOW);
        assertNull(job.getApprovedAt());
        assertFalse(job.isReadyToPublish(TODAY));
    }

    @Test
    void salaryAndDeadlineValidation() {
        assertEquals("JOB_R2", rule(() -> new SalaryRange(new BigDecimal("10"), new BigDecimal("5"), null)));
        assertThrows(IllegalArgumentException.class, () -> new SalaryRange(new BigDecimal("1.234"), null, null));
        assertEquals("JOB_R3", rule(() -> Job.createDraft(details("Java", false, TODAY.minusDays(1)), 5L, "5",
                TODAY, NOW)));
    }

    @Test
    void closesAt2359OnTheDeadlineAndAcceptsApplicationsUntilThen() {
        Job job = Job.createDraft(details("Java Dev", false, TODAY), 5L, "5", TODAY, NOW);
        job.publish("5", TODAY, NOW);
        assertTrue(job.isAcceptingApplications(TODAY));
        assertFalse(job.isAcceptingApplications(TODAY.plusDays(1)));

        assertFalse(job.closeIfExpired(LocalDateTime.of(TODAY, java.time.LocalTime.of(0, 59)), "system", NOW));
        assertFalse(job.closeIfExpired(LocalDateTime.of(TODAY, java.time.LocalTime.of(23, 58)), "system", NOW));
        assertTrue(job.closeIfExpired(LocalDateTime.of(TODAY, java.time.LocalTime.of(23, 59)), "system", NOW));
        assertEquals(JobStatus.CLOSED, job.getStatus());

        job.reopen("5", NOW);
        assertEquals(JobStatus.DRAFT, job.getStatus());
    }

    @Test
    void onlyDraftsWithoutApplicationsCanBeDeleted() {
        Job job = Job.createDraft(details("Java Dev", false, null), 5L, "5", TODAY, NOW);
        assertEquals("JOB_R7", rule(() -> job.markDeleted(1, "5", NOW)));
        job.publish("5", TODAY, NOW);
        assertEquals("JOB_R7", rule(() -> job.markDeleted(0, "5", NOW)));
        job.unpublish("5", NOW);
        job.markDeleted(0, "5", NOW);
        assertTrue(job.isDeleted());
    }

    @Test
    void descriptionIsSanitized() {
        JobDetails withScript = new JobDetails("T", 1L, null, null, 1, null,
                "<p>Hi</p><script>alert(1)</script>", Set.of(), null, null, null, true, null, null);
        assertEquals("<p>Hi</p>", withScript.description());
    }
}
