package com.ats;

import com.ats.auth.application.port.out.AccessTokenIssuer;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Job requisition -> approval -> Career Site -> application with CV -> background parsing -> candidate search,
 * with role checks and recruiter data isolation (BRULE-01).
 */
@SpringBootTest
@AutoConfigureMockMvc
class JobCandidateFlowIntegrationTest {

    private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccessTokenIssuer tokens;

    private final String run = UUID.randomUUID().toString().substring(0, 8);

    private String token(long userId, String role) {
        return tokens.issue(userId, List.of(role)).token();
    }

    private static MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    private static long id(String json, String path) {
        return ((Number) JsonPath.read(json, path)).longValue();
    }

    private static byte[] resource(String name) throws IOException {
        try (InputStream in = JobCandidateFlowIntegrationTest.class.getResourceAsStream("/cv/" + name)) {
            return in.readAllBytes();
        }
    }

    private long createUser(String admin, String name, String role) throws Exception {
        String json = mvc.perform(as(admin, post("/api/v1/users/local")).content("""
                        {"fullName": "%s", "email": "%s-%s@ats.dev", "role": "%s"}
                        """.formatted(name, name.toLowerCase().replace(' ', '.'), run, role)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return id(json, "$.data.id");
    }

    private long skillId(String token, String name) throws Exception {
        String json = mvc.perform(as(token, get("/api/v1/skills")).param("keyword", name))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(json, "$.data[?(@.name == '" + name + "')].id");
        return ids.get(0).longValue();
    }

    private MockMultipartHttpServletRequestBuilder apply(long jobId, String email, MockMultipartFile cv) {
        MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/public/jobs/{id}/applications", jobId);
        if (cv != null) {
            request.file(cv);
        }
        request.param("fullName", "Nguyen Van An").param("email", email).param("phone", "+84 987 654 321")
                .param("source", "LINKEDIN").param("utmSource", "linkedin").param("coverLetter", "Hello");
        return request;
    }

    @Test
    void jobRequisitionApplicationAndCandidateDatabase() throws Exception {
        String admin = token(990, "ADMIN");
        long recruiterId = createUser(admin, "Recruiter One", "RECRUITER");
        long otherRecruiterId = createUser(admin, "Recruiter Two", "RECRUITER");
        long managerId = createUser(admin, "Manager One", "HIRING_MANAGER");
        String recruiter = token(recruiterId, "RECRUITER");
        String otherRecruiter = token(otherRecruiterId, "RECRUITER");
        String manager = token(managerId, "HIRING_MANAGER");
        String candidateUser = token(5000, "CANDIDATE");

        String deptJson = mvc.perform(as(admin, post("/api/v1/departments")).content("{\"name\": \"Eng " + run + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long departmentId = id(deptJson, "$.data.id");
        long java = skillId(recruiter, "Java");
        long spring = skillId(recruiter, "Spring Boot");

        // role checks
        mvc.perform(as(candidateUser, get("/api/v1/jobs"))).andExpect(status().isForbidden());
        mvc.perform(as(recruiter, post("/api/v1/skills")).content("{\"name\": \"Rust\"}"))
                .andExpect(status().isForbidden());

        // 1. Save Draft (only title), Save & Submit reports the missing fields
        String draft = mvc.perform(as(recruiter, post("/api/v1/jobs")).content("{\"title\": \"Java Dev " + run + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Job requisition has been saved as draft."))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.recruiterId").value(recruiterId))
                .andReturn().getResponse().getContentAsString();
        long jobId = id(draft, "$.data.id");
        mvc.perform(as(recruiter, post("/api/v1/jobs/{id}/submit", jobId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.rule").value("JOB_R1"))
                .andExpect(jsonPath("$.data.fields", hasItems("departmentId", "description", "skillIds",
                        "hiringManagerId")));

        // 2. Complete the requisition (salary rule checked), submit for approval
        String form = """
                {"title": "Java Dev %s", "departmentId": %d, "employmentType": "FULL_TIME", "location": "Hanoi",
                 "salaryMin": 1000, "salaryMax": %d, "salaryCurrency": "USD",
                 "description": "<h2>Role</h2><p>Build APIs</p><script>alert(1)</script>",
                 "skillIds": [%d, %d], "hiringManagerId": %d, "requiresApproval": true}
                """;
        mvc.perform(as(recruiter, put("/api/v1/jobs/{id}", jobId))
                        .content(form.formatted(run, departmentId, 500, java, spring, managerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.rule").value("JOB_R2"));
        mvc.perform(as(recruiter, put("/api/v1/jobs/{id}", jobId))
                        .content(form.formatted(run, departmentId, 2000, java, spring, managerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("<h2>Role</h2><p>Build APIs</p>"))
                .andExpect(jsonPath("$.data.department.name").value("Eng " + run));
        mvc.perform(as(otherRecruiter, get("/api/v1/jobs/{id}", jobId))).andExpect(status().isForbidden());
        mvc.perform(as(recruiter, post("/api/v1/jobs/{id}/submit", jobId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

        // 3. BRULE-11: publish needs the hiring manager's approval
        mvc.perform(as(recruiter, post("/api/v1/jobs/{id}/publish", jobId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.rule").value("JOB_R5"));
        mvc.perform(as(recruiter, post("/api/v1/jobs/{id}/approve", jobId))).andExpect(status().isForbidden());
        mvc.perform(as(manager, get("/api/v1/jobs")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[*].id", hasItem((int) jobId)));
        mvc.perform(as(manager, post("/api/v1/jobs/{id}/approve", jobId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readyToPublish").value(true));
        mvc.perform(as(recruiter, post("/api/v1/jobs/{id}/publish", jobId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        // BRULE-13: title locked while published
        mvc.perform(as(recruiter, put("/api/v1/jobs/{id}", jobId))
                        .content(form.formatted("Renamed", departmentId, 2000, java, spring, managerId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.rule").value("JOB_R4"));

        // 4. Career Site (no token)
        mvc.perform(get("/api/v1/public/jobs").param("keyword", "spring boot").param("departmentId",
                        String.valueOf(departmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].excerpt").value("Role Build APIs"))
                .andExpect(jsonPath("$.data.items[0].skills", hasItems("Java", "Spring Boot")));
        mvc.perform(get("/api/v1/public/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acceptingApplications").value(true))
                .andExpect(jsonPath("$.data.salaryCurrency").value("USD"));
        mvc.perform(get("/api/v1/public/jobs/filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.locations", hasItem("Hanoi")));

        // 5. Apply with a DOCX CV
        String email = "an-" + run + "@example.com";
        MockMultipartFile docx = new MockMultipartFile("cv", "my-cv.docx", DOCX, resource("sample-cv.docx"));
        String applied = mvc.perform(apply(jobId, email, docx))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.applicationCode").value(matchesPattern("APP-\\d{4}-\\d{5}")))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("Your application has been submitted successfully! Application ID: "
                + JsonPath.read(applied, "$.data.applicationCode"), JsonPath.read(applied, "$.message"));

        // BRULE-09, BRULE-08, required fields
        mvc.perform(apply(jobId, email.toUpperCase(), docx))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.rule").value("APPLICATION_R1"));
        mvc.perform(apply(jobId, "x-" + email, new MockMultipartFile("cv", "photo.jpg", "image/jpeg",
                        new byte[]{(byte) 0xFF, (byte) 0xD8, 1, 2})))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid format. Only PDF and DOCX files are supported."));
        byte[] big = new byte[6 * 1024 * 1024];
        System.arraycopy("%PDF-1.4".getBytes(), 0, big, 0, 8);
        mvc.perform(apply(jobId, "y-" + email, new MockMultipartFile("cv", "big.pdf", "application/pdf", big)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.rule").value("CV_R2"));
        mvc.perform(multipart("/api/v1/public/jobs/{id}/applications", jobId).param("email", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data[*].field", hasItems("fullName", "email", "cv")));

        // 6. The CV is parsed in the background; the recruiter finds the candidate by skill keyword
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                mvc.perform(as(recruiter, get("/api/v1/candidates")).param("keyword", "kubernetes")
                                .param("jobId", String.valueOf(jobId)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.totalElements").value(1))
                        .andExpect(jsonPath("$.data.items[0].latestCv.parseStatus").value("PARSED")));
        String found = mvc.perform(as(recruiter, get("/api/v1/candidates")).param("keyword", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].fullName").value("Nguyen Van An"))
                .andExpect(jsonPath("$.data.items[0].skills", hasItems("Java", "Spring Boot", "Kubernetes")))
                .andExpect(jsonPath("$.data.items[0].applicationCount").value(1))
                .andReturn().getResponse().getContentAsString();
        long candidateId = id(found, "$.data.items[0].id");
        long cvId = id(found, "$.data.items[0].latestCv.id");

        // BRULE-01: another recruiter does not see the candidate; the job's hiring manager does
        mvc.perform(as(otherRecruiter, get("/api/v1/candidates")).param("keyword", email))
                .andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(as(otherRecruiter, get("/api/v1/candidates/{id}", candidateId))).andExpect(status().isNotFound());
        mvc.perform(as(manager, get("/api/v1/candidates/{id}", candidateId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.cvs[0].parsedData.experience[0]")
                        .value("Senior Java Developer tại Công ty XYZ (2019 - nay)"))
                .andExpect(jsonPath("$.data.applications[0].jobTitle").value("Java Dev " + run));
        byte[] downloaded = mvc.perform(as(recruiter, get("/api/v1/candidates/{id}/cvs/{cvId}/file", candidateId, cvId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(resource("sample-cv.docx"), downloaded);

        // the job list shows the application; the job's applications list
        mvc.perform(as(recruiter, get("/api/v1/jobs")).param("status", "PUBLISHED"))
                .andExpect(jsonPath("$.data.items[?(@.id == " + jobId + ")].applicationCount", hasItem(1)));
        mvc.perform(as(recruiter, get("/api/v1/applications")).param("jobId", String.valueOf(jobId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].candidateEmail").value(email));
        mvc.perform(as(recruiter, get("/api/v1/jobs/stats")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.published").value(1));

        // 7. Recruiter bulk import: one PDF accepted, one invalid file rejected; the PDF fills the profile
        String imported = mvc.perform(multipart("/api/v1/candidates/import")
                        .file(new MockMultipartFile("files", "ngoc.pdf", "application/pdf", resource("sample-cv.pdf")))
                        .file(new MockMultipartFile("files", "notes.txt", "text/plain", "hello".getBytes()))
                        .param("jobId", String.valueOf(jobId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imported").value(1))
                .andExpect(jsonPath("$.data.failed").value(1))
                .andReturn().getResponse().getContentAsString();
        long importedCandidate = id(imported, "$.data.results[0].candidateId");
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                mvc.perform(as(recruiter, get("/api/v1/candidates/{id}", importedCandidate)))
                        .andExpect(jsonPath("$.data.fullName").value("Tran Thi Bich Ngoc"))
                        .andExpect(jsonPath("$.data.email").value("ngoc.tran@example.com"))
                        .andExpect(jsonPath("$.data.skills[*].name", hasItems("Java", "PostgreSQL", "Docker"))));

        // 8. Unpublished jobs disappear from the Career Site and stop accepting applications
        mvc.perform(as(recruiter, post("/api/v1/jobs/{id}/unpublish", jobId))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/public/jobs/{id}", jobId)).andExpect(status().isNotFound());
        mvc.perform(apply(jobId, "late-" + email, docx)).andExpect(status().isNotFound());
        mvc.perform(as(recruiter, org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/jobs/{id}", jobId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.rule").value("JOB_R7"));
    }
}
