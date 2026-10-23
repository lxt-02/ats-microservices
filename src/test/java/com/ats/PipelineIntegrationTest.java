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

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recruitment pipeline over HTTP: Kanban board, BRULE-12 moves, notes, transfer (BRULE-04), hiring manager
 * decision (BRULE-05), offer outcome, reopen, assignment status and Kanban stage settings.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PipelineIntegrationTest {

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

    private String ok(org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
        return mvc.perform(request).andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString();
    }

    private long user(String admin, String name, String role) throws Exception {
        return id(ok(as(admin, post("/api/v1/users/local")).content("""
                {"fullName": "%s", "email": "%s-%s@ats.dev", "role": "%s"}
                """.formatted(name, name.toLowerCase().replace(' ', '.'), run, role))), "$.data.id");
    }

    private long publishedJob(String recruiter, String title, long departmentId, long managerId, long skillId)
            throws Exception {
        long jobId = id(ok(as(recruiter, post("/api/v1/jobs")).content("""
                {"title": "%s %s", "departmentId": %d, "description": "<p>Work</p>", "skillIds": [%d],
                 "hiringManagerId": %d, "requiresApproval": false}
                """.formatted(title, run, departmentId, skillId, managerId))), "$.data.id");
        ok(as(recruiter, post("/api/v1/jobs/{id}/publish", jobId)));
        return jobId;
    }

    private long apply(long jobId, String email) throws Exception {
        byte[] cv;
        try (InputStream in = getClass().getResourceAsStream("/cv/sample-cv.docx")) {
            cv = in.readAllBytes();
        }
        return id(ok(multipart("/api/v1/public/jobs/{id}/applications", jobId)
                .file(new MockMultipartFile("cv", "cv.docx", DOCX, cv))
                .param("fullName", "Candidate " + email).param("email", email)), "$.data.applicationId");
    }

    private MockHttpServletRequestBuilder move(String token, long applicationId, String to) {
        return as(token, post("/api/v1/applications/{id}/move", applicationId))
                .content("{\"toStatus\": \"" + to + "\", \"note\": \"moved to " + to + "\"}");
    }

    @Test
    void recruitmentPipeline() throws Exception {
        String admin = token(991, "ADMIN");
        long recruiterId = user(admin, "Pipeline Recruiter", "RECRUITER");
        long otherRecruiterId = user(admin, "Other Recruiter", "RECRUITER");
        long managerId = user(admin, "Pipeline Manager", "HIRING_MANAGER");
        String recruiter = token(recruiterId, "RECRUITER");
        String otherRecruiter = token(otherRecruiterId, "RECRUITER");
        String manager = token(managerId, "HIRING_MANAGER");
        long departmentId = id(ok(as(admin, post("/api/v1/departments")).content("{\"name\": \"Eng " + run + "\"}")),
                "$.data.id");
        long teamId = id(ok(as(admin, post("/api/v1/departments")).content(
                "{\"name\": \"Platform Team " + run + "\", \"parentId\": " + departmentId + "}")), "$.data.id");
        List<Number> skills = JsonPath.read(ok(as(recruiter, get("/api/v1/skills")).param("keyword", "Java")),
                "$.data[*].id");
        long skill = skills.get(0).longValue();

        long backendJob = publishedJob(recruiter, "Backend", departmentId, managerId, skill);
        long frontendJob = publishedJob(recruiter, "Frontend", departmentId, managerId, skill);
        long first = apply(backendJob, "first-" + run + "@example.com");
        long second = apply(backendJob, "second-" + run + "@example.com");

        // Kanban board: both cards in the "New" column, columns in stage order
        mvc.perform(as(manager, get("/api/v1/pipeline/board")).param("jobId", String.valueOf(backendJob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.columns[0].status").value("NEW"))
                .andExpect(jsonPath("$.data.columns[0].count").value(2))
                .andExpect(jsonPath("$.data.columns[*].status", hasItems("CV_SCREENING", "OFFERED", "REJECTED")));
        mvc.perform(as(otherRecruiter, get("/api/v1/pipeline/board")).param("jobId", String.valueOf(backendJob)))
                .andExpect(status().isForbidden());

        // BRULE-12: no skipping; only the job's recruiter moves cards
        mvc.perform(move(recruiter, first, "CV_PASSED"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.rule").value("APPLICATION_R3"))
                .andExpect(jsonPath("$.data.allowed", hasItems("CV_SCREENING", "REJECTED", "TRANSFERRED")));
        mvc.perform(move(otherRecruiter, first, "CV_SCREENING")).andExpect(status().isForbidden());
        mvc.perform(move(manager, first, "CV_SCREENING")).andExpect(status().isForbidden());
        mvc.perform(move(recruiter, first, "CV_SCREENING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CV_SCREENING"))
                .andExpect(jsonPath("$.data.stageName").value("CV Reviewing"))
                .andExpect(jsonPath("$.data.canTransfer").value(true));

        // Evaluation notes by the recruiter and the hiring manager
        mvc.perform(as(recruiter, post("/api/v1/applications/{id}/notes", first))
                        .content("{\"content\": \"Good Spring Boot experience\"}"))
                .andExpect(status().isCreated());
        mvc.perform(as(manager, post("/api/v1/applications/{id}/notes", first))
                        .content("{\"content\": \"Ask about Kafka\"}"))
                .andExpect(status().isCreated());
        mvc.perform(as(recruiter, get("/api/v1/applications/{id}/notes", first)))
                .andExpect(jsonPath("$.data[0].content").value("Ask about Kafka"))
                .andExpect(jsonPath("$.data[0].authorId").value(managerId));

        // Transfer during Round 1 (BRULE-04); the target job gets a NEW application linked to its source
        String transferred = ok(as(recruiter, post("/api/v1/applications/{id}/transfer", second))
                .content("{\"targetJobId\": " + frontendJob + "}"));
        long copy = id(transferred, "$.data.targetApplicationId");
        mvc.perform(as(recruiter, get("/api/v1/applications/{id}", second)))
                .andExpect(jsonPath("$.data.status").value("TRANSFERRED"));
        mvc.perform(as(recruiter, get("/api/v1/applications/{id}", copy)))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andExpect(jsonPath("$.data.jobId").value(frontendJob))
                .andExpect(jsonPath("$.data.transferredFromId").value(second));
        mvc.perform(as(recruiter, post("/api/v1/applications/{id}/transfer", second))
                        .content("{\"targetJobId\": " + frontendJob + "}"))
                .andExpect(status().isConflict());
        mvc.perform(as(recruiter, get("/api/v1/pipeline/board")).param("jobId", String.valueOf(frontendJob)))
                .andExpect(jsonPath("$.data.columns[0].cards[0].transferred").value(true));

        // Through the technical round; transfer is no longer possible after Round 1
        for (String stage : List.of("CV_PASSED", "TECHNICAL_INTERVIEW", "TECH_PASSED")) {
            mvc.perform(move(recruiter, first, stage)).andExpect(status().isOk());
        }
        mvc.perform(as(recruiter, post("/api/v1/applications/{id}/transfer", first))
                        .content("{\"targetJobId\": " + frontendJob + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.rule").value("APPLICATION_R4"));
        mvc.perform(move(recruiter, first, "HM_INTERVIEW"))
                .andExpect(jsonPath("$.data.canDecide").value(false));

        // Offered only through the hiring manager's decision, with a target team (BRULE-05)
        mvc.perform(move(recruiter, first, "OFFERED")).andExpect(status().isConflict());
        mvc.perform(as(recruiter, post("/api/v1/applications/{id}/hm-decision", first))
                        .content("{\"decision\": \"APPROVE\", \"teamId\": " + teamId + "}"))
                .andExpect(status().isForbidden());
        mvc.perform(as(manager, get("/api/v1/applications/{id}", first)))
                .andExpect(jsonPath("$.data.canDecide").value(true))
                .andExpect(jsonPath("$.data.allowedMoves").isEmpty());
        mvc.perform(as(manager, post("/api/v1/applications/{id}/hm-decision", first))
                        .content("{\"decision\": \"APPROVE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.rule").value("APPLICATION_R6"));
        mvc.perform(as(manager, post("/api/v1/applications/{id}/hm-decision", first))
                        .content("{\"decision\": \"APPROVE\", \"teamId\": " + teamId + ", \"comment\": \"Strong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OFFERED"))
                .andExpect(jsonPath("$.data.hiringDecision.team.name").value("Platform Team " + run))
                .andExpect(jsonPath("$.data.hiringDecision.decidedBy").value(managerId));

        // UC-02.16 assignment status, limited to the recruiter's jobs
        mvc.perform(as(recruiter, get("/api/v1/applications/assignments")))
                .andExpect(jsonPath("$.data[*].applicationId", hasItem((int) first)))
                .andExpect(jsonPath("$.data[0].team.id").value(teamId));
        mvc.perform(as(otherRecruiter, get("/api/v1/applications/assignments")))
                .andExpect(jsonPath("$.data").isEmpty());

        // Offer declined, reopened (back to Offered), then accepted
        mvc.perform(as(recruiter, post("/api/v1/applications/{id}/offer-outcome", first))
                        .content("{\"accepted\": false}"))
                .andExpect(jsonPath("$.data.status").value("OFFER_DECLINED"))
                .andExpect(jsonPath("$.data.canReopen").value(true));
        mvc.perform(as(recruiter, post("/api/v1/applications/{id}/reopen", first)).content("{}"))
                .andExpect(jsonPath("$.data.status").value("OFFERED"));
        mvc.perform(as(recruiter, post("/api/v1/applications/{id}/offer-outcome", first))
                        .content("{\"accepted\": true}"))
                .andExpect(jsonPath("$.data.status").value("HIRED"))
                .andExpect(jsonPath("$.data.transitions[*].toStatus", contains("NEW", "CV_SCREENING", "CV_PASSED",
                        "TECHNICAL_INTERVIEW", "TECH_PASSED", "HM_INTERVIEW", "OFFERED", "OFFER_DECLINED", "OFFERED",
                        "HIRED")));

        // Rejection at Round 1 still allows reopening (and transfer)
        mvc.perform(move(recruiter, copy, "REJECTED"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.canTransfer").value(true));
        mvc.perform(as(recruiter, post("/api/v1/applications/{id}/reopen", copy)).content("{}"))
                .andExpect(jsonPath("$.data.status").value("NEW"));

        // UC-04.3 Kanban stage settings: admin renames a column, recruiters cannot
        List<Number> stageIds = JsonPath.read(ok(as(recruiter, get("/api/v1/pipeline/stages"))),
                "$.data[?(@.status == 'CV_SCREENING')].id");
        long screening = stageIds.get(0).longValue();
        mvc.perform(as(recruiter, put("/api/v1/pipeline/stages/{id}", screening))
                        .content("{\"name\": \"Screening\", \"order\": 2, \"color\": \"#000000\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(as(admin, put("/api/v1/pipeline/stages/{id}", screening))
                        .content("{\"name\": \"Screening\", \"order\": 2, \"color\": \"#123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Screening"));
        mvc.perform(as(recruiter, get("/api/v1/pipeline/board")).param("jobId", String.valueOf(backendJob)))
                .andExpect(jsonPath("$.data.columns[1].name").value("Screening"));
        mvc.perform(as(admin, put("/api/v1/pipeline/stages/{id}", screening))
                        .content("{\"name\": \"CV Reviewing\", \"order\": 2, \"color\": \"#0ea5e9\"}"))
                .andExpect(status().isOk());
    }
}
