package com.ats;

import com.ats.auth.application.port.out.AccessTokenIssuer;
import com.ats.auth.application.response.AuthTokenResponse;
import com.ats.auth.application.service.AuthSessionService;
import com.ats.user.application.contract.UserAccountContract;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Whole application over HTTP: authentication filter, controllers, use cases, MyBatis and Flyway on H2. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final long ADMIN_ID = 999L;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccessTokenIssuer accessTokenIssuer;

    @Autowired
    private AuthSessionService authSessionService;

    @Autowired
    private UserAccountContract userAccounts;

    private String adminToken() {
        return accessTokenIssuer.issue(ADMIN_ID, List.of("ADMIN")).token();
    }

    private MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken())
                .contentType(MediaType.APPLICATION_JSON);
    }

    private long createDepartment(String body) throws Exception {
        String json = mvc.perform(asAdmin(post("/api/v1/departments")).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("OK"))
                .andExpect(jsonPath("$.code").value(201))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.data.id")).longValue();
    }

    private long createUser(String email, Long departmentId) throws Exception {
        String body = """
                {"departmentId": %s, "fullName": "Nguyen Van A", "email": "%s", "role": "RECRUITER"}
                """.formatted(departmentId, email);
        String json = mvc.perform(asAdmin(post("/api/v1/users/local")).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.data.id")).longValue();
    }

    @Test
    void protectedEndpointsRequireAValidAccessToken() throws Exception {
        mvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("NG"))
                .andExpect(jsonPath("$.code").value(401));

        // Identity headers sent by a client are ignored: only the token counts.
        mvc.perform(get("/api/v1/users/1").header("X-User-Id", "1").header("X-User-Roles", "ADMIN"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/users/1").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicEndpointsDoNotRequireAToken() throws Exception {
        mvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"unknown\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void departmentAndUserLifecycle() throws Exception {
        long root = createDepartment("{\"name\": \"Engineering\"}");
        long child = createDepartment("{\"name\": \"Backend\", \"parentId\": %d}".formatted(root));

        mvc.perform(asAdmin(get("/api/v1/departments")).param("parentId", String.valueOf(root)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Backend"));

        // DEPARTMENT_R1: a department cannot be its own parent
        mvc.perform(asAdmin(put("/api/v1/departments/" + root))
                        .content("{\"name\": \"Engineering\", \"parentId\": %d}".formatted(root)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.rule").value("DEPARTMENT_R1"));

        long userId = createUser("Recruiter@Example.com", child);
        mvc.perform(asAdmin(get("/api/v1/users/" + userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("recruiter@example.com"))
                .andExpect(jsonPath("$.data.departmentId").value(child))
                .andExpect(jsonPath("$.data.role").value("RECRUITER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.createdBy").value(String.valueOf(ADMIN_ID)))
                .andExpect(jsonPath("$.data.createdAt").exists());

        mvc.perform(asAdmin(get("/api/v1/users/by-email")).param("email", "recruiter@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId));

        // duplicate email -> 409
        mvc.perform(asAdmin(post("/api/v1/users/local"))
                        .content("{\"fullName\": \"Other\", \"email\": \"recruiter@example.com\"}"))
                .andExpect(status().isConflict());

        // unknown department -> 404
        mvc.perform(asAdmin(post("/api/v1/users/local"))
                        .content("{\"fullName\": \"Other\", \"email\": \"o@example.com\", \"departmentId\": 424242}"))
                .andExpect(status().isNotFound());

        // validation -> 400 with field errors
        mvc.perform(asAdmin(post("/api/v1/users/local")).content("{\"email\": \"bad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("NG"))
                .andExpect(jsonPath("$.data[?(@.field == 'fullName')].code").value("NotBlank"))
                .andExpect(jsonPath("$.data[?(@.field == 'email')].code").value("Email"));

        mvc.perform(asAdmin(patch("/api/v1/users/" + userId + "/status")).content("{\"status\": \"LOCKED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LOCKED"));

        mvc.perform(asAdmin(delete("/api/v1/users/" + userId))).andExpect(status().isNoContent());
        mvc.perform(asAdmin(get("/api/v1/users/" + userId))).andExpect(status().isNotFound());

        mvc.perform(asAdmin(get("/api/v1/does-not-exist")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("NG"));
    }

    @Test
    void refreshTokenRotationAgainstTheDatabase() throws Exception {
        long userId = createUser("session@example.com", null);
        AuthTokenResponse session = authSessionService.open(userAccounts.findById(userId).orElseThrow());

        String json = mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"%s\"}".formatted(session.refreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        String newAccessToken = JsonPath.read(json, "$.data.accessToken");
        String newRefreshToken = JsonPath.read(json, "$.data.refreshToken");

        // the issued access token is accepted by the filter and identifies the user
        mvc.perform(get("/api/v1/users/" + userId).header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("session@example.com"));

        // reusing the rotated token revokes every session of the user
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"%s\"}".formatted(session.refreshToken())))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"%s\"}".formatted(newRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ssoConfigurationLifecycle() throws Exception {
        String json = mvc.perform(asAdmin(post("/api/v1/sso-configurations")).content("""
                        {"providerType": "GOOGLE", "clientId": "client", "issuerUri": "https://accounts.google.com",
                         "scopes": ["openid", "email"]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(json, "$.data.id")).longValue();

        mvc.perform(asAdmin(get("/api/v1/sso-configurations/active/google")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scopes.length()").value(2));

        mvc.perform(asAdmin(delete("/api/v1/sso-configurations/" + id))).andExpect(status().isNoContent());
        mvc.perform(asAdmin(get("/api/v1/sso-configurations/" + id))).andExpect(status().isNotFound());
    }
}
