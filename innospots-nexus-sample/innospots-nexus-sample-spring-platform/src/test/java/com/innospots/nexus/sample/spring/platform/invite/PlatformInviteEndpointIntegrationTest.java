package com.innospots.nexus.sample.spring.platform.invite;

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
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_INVITE_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformInviteEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-invite";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanInvites(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of("nx_pl_invite", "invite_id"));
    }

    @Test
    void inviteEndpointsCreateResendAndRevokePersistRows() throws Exception {
        String createBody = """
                {
                  "email": "invite.it@example.com",
                  "loginName": "it.invited.user",
                  "validityDays": 7,
                  "deliveryMode": "OFFLINE"
                }
                """;
        HttpResponse<String> created = http.postJson(PlatformConstant.INVITES_PATH, createBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        assertThat(created.body()).contains("invite.it@example.com");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-invite-created",
                Map.of("nx_pl_invite", "invite_id"));

        String inviteId = jdbcTemplate.queryForObject(
                "SELECT invite_id FROM nx_pl_invite WHERE login_name = ?",
                String.class,
                "it.invited.user");

        HttpResponse<String> got = http.get(PlatformConstant.INVITES_PATH + "/" + inviteId);
        assertThat(got.statusCode()).isEqualTo(200);
        assertThat(got.body()).contains("OFFLINE");

        HttpResponse<String> paged = http.get(PlatformConstant.INVITES_PATH + "?pageNo=1&pageSize=20");
        assertThat(paged.statusCode()).isEqualTo(200);
        assertThat(paged.body()).contains(inviteId);

        HttpResponse<String> resent = http.postJson(PlatformConstant.INVITES_PATH + "/" + inviteId + "/resend", null);
        assertThat(resent.statusCode()).as(resent.body()).isEqualTo(200);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-resend",
                Map.of("nx_pl_invite", "invite_id"));

        HttpResponse<String> revoked = http.postJson(PlatformConstant.INVITES_PATH + "/" + inviteId + "/revoke", null);
        assertThat(revoked.statusCode()).as(revoked.body()).isEqualTo(200);

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM nx_pl_invite WHERE invite_id = ?",
                String.class,
                inviteId);
        assertThat(status).isEqualTo("REVOKED");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "03-after-revoke",
                Map.of("nx_pl_invite", "invite_id"));
    }
}
