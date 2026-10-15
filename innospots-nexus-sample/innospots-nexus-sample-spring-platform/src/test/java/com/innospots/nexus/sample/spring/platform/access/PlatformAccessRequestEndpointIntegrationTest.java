package com.innospots.nexus.sample.spring.platform.access;

import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.support.PlatformDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_ACCESS_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformAccessRequestEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-access";

    private static final String APPROVE_USER_ID = "it-pending-user-approve-00001";

    private static final String APPROVE_REQUEST_ID = "it-access-request-approve-01";

    private static final String REJECT_USER_ID = "it-pending-user-reject-00001";

    private static final String REJECT_REQUEST_ID = "it-access-request-reject-01";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanAccessRequests(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of(
                        "nx_pl_access_request",
                        "access_request_id",
                        "nx_pl_user",
                        "platform_user_id"));
    }

    @Test
    void approveAccessRequestActivatesPendingUser() throws Exception {
        PlatformIntegrationTestSupport.insertPendingApprovalUser(jdbcTemplate, APPROVE_USER_ID, "it.approve.user");
        PlatformIntegrationTestSupport.insertPendingAccessRequest(
                jdbcTemplate,
                APPROVE_REQUEST_ID,
                APPROVE_USER_ID,
                "it.approve.user",
                "IT Approve User");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "approve-01-seeded",
                Map.of("nx_pl_access_request", "access_request_id", "nx_pl_user", "platform_user_id"));

        HttpResponse<String> paged = http.get(PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH + "?pageNo=1&pageSize=20");
        assertThat(paged.statusCode()).as(paged.body()).isEqualTo(200);
        assertThat(paged.body()).contains(APPROVE_REQUEST_ID);

        HttpResponse<String> got = http.get(
                PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH + "/" + APPROVE_REQUEST_ID);
        assertThat(got.statusCode()).isEqualTo(200);
        assertThat(got.body()).contains("PENDING_APPROVAL");

        HttpResponse<String> approved = http.postJson(
                PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH + "/" + APPROVE_REQUEST_ID + "/approve",
                "{}");
        assertThat(approved.statusCode()).as(approved.body()).isEqualTo(200);
        assertThat(approved.body()).contains(APPROVE_USER_ID);

        String userStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM nx_pl_user WHERE platform_user_id = ?",
                String.class,
                APPROVE_USER_ID);
        assertThat(userStatus).isEqualTo("ACTIVE");

        String requestStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM nx_pl_access_request WHERE access_request_id = ?",
                String.class,
                APPROVE_REQUEST_ID);
        assertThat(requestStatus).isEqualTo("APPROVED");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "approve-02-after-approve",
                Map.of("nx_pl_access_request", "access_request_id", "nx_pl_user", "platform_user_id"));
    }

    @Test
    void rejectAccessRequestDisablesPendingUser() throws Exception {
        PlatformIntegrationTestSupport.insertPendingApprovalUser(jdbcTemplate, REJECT_USER_ID, "it.reject.user");
        PlatformIntegrationTestSupport.insertPendingAccessRequest(
                jdbcTemplate,
                REJECT_REQUEST_ID,
                REJECT_USER_ID,
                "it.reject.user",
                "IT Reject User");

        String rejectBody = """
                {
                  "rejectReason": "Not eligible for platform access"
                }
                """;
        HttpResponse<String> rejected = http.postJson(
                PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH + "/" + REJECT_REQUEST_ID + "/reject",
                rejectBody);
        assertThat(rejected.statusCode()).as(rejected.body()).isEqualTo(200);

        String userStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM nx_pl_user WHERE platform_user_id = ?",
                String.class,
                REJECT_USER_ID);
        assertThat(userStatus).isEqualTo("DISABLED");

        String requestStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM nx_pl_access_request WHERE access_request_id = ?",
                String.class,
                REJECT_REQUEST_ID);
        assertThat(requestStatus).isEqualTo("REJECTED");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "reject-01-after-reject",
                Map.of("nx_pl_access_request", "access_request_id", "nx_pl_user", "platform_user_id"));
    }
}
