package com.innospots.nexus.sample.spring.platform.user;

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
import com.innospots.nexus.sample.spring.platform.support.PlatformSampleRsaPassword;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_USER_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformUserEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-user";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanPlatformUsers(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of("nx_pl_user", "login_name", "nx_user_credential", "subject_id"));
    }

    @Test
    void userEndpointsCreateUpdateAndDisablePersistRows() throws Exception {
        String encryptedPassword = PlatformSampleRsaPassword.encrypt("ItUser-Pass-9!");
        String createBody = """
                {
                  "loginName": "it.platform.user",
                  "displayName": "IT Platform User",
                  "email": "it.platform.user@example.com",
                  "encryptedPassword": "%s"
                }
                """.formatted(encryptedPassword);

        HttpResponse<String> created = http.postJson(PlatformConstant.USERS_PATH, createBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        assertThat(created.body()).contains("it.platform.user");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-user-created",
                Map.of("nx_pl_user", "login_name", "nx_user_credential", "subject_id"));

        String platformUserId = jdbcTemplate.queryForObject(
                "SELECT platform_user_id FROM nx_pl_user WHERE login_name = ?",
                String.class,
                "it.platform.user");

        Long credentialRows = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM nx_user_credential
                WHERE subject_id = ? AND security_realm = 'PLATFORM' AND credential_kind = 'PASSWORD'
                """,
                Long.class,
                platformUserId);
        assertThat(credentialRows).isEqualTo(1L);

        HttpResponse<String> got = http.get(PlatformConstant.USERS_PATH + "/" + platformUserId);
        assertThat(got.statusCode()).isEqualTo(200);
        assertThat(got.body()).contains("IT Platform User");

        String updateBody = """
                {
                  "displayName": "IT Platform User Updated"
                }
                """;
        HttpResponse<String> updated = http.putJson(PlatformConstant.USERS_PATH + "/" + platformUserId, updateBody);
        assertThat(updated.statusCode()).as(updated.body()).isEqualTo(200);
        assertThat(updated.body()).contains("IT Platform User Updated");

        String disableBody = """
                {
                  "status": "DISABLED"
                }
                """;
        HttpResponse<String> disabled = http.putJson(
                PlatformConstant.USERS_PATH + "/" + platformUserId + "/status",
                disableBody);
        assertThat(disabled.statusCode()).as(disabled.body()).isEqualTo(200);

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM nx_pl_user WHERE platform_user_id = ?",
                String.class,
                platformUserId);
        assertThat(status).isEqualTo("DISABLED");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-user-disabled",
                Map.of("nx_pl_user", "login_name"));
    }
}
