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
import com.innospots.nexus.sample.spring.platform.support.PlatformSampleRsaPassword;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_PUBLIC_INVITE_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformPublicInviteEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-public-invite";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanInvites(jdbcTemplate);
        PlatformIntegrationTestSupport.cleanPlatformUsers(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of("nx_pl_invite", "invite_id", "nx_pl_user", "login_name"));
    }

    @Test
    void publicInvitePreviewAndOfflineAcceptCreatesUser() throws Exception {
        String createBody = """
                {
                  "email": "public.invite@example.com",
                  "loginName": "it.public.invited",
                  "validityDays": 7,
                  "deliveryMode": "OFFLINE"
                }
                """;
        HttpResponse<String> created = http.postJson(PlatformConstant.INVITES_PATH, createBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);

        String inviteToken = jdbcTemplate.queryForObject(
                "SELECT invite_token FROM nx_pl_invite WHERE login_name = ?",
                String.class,
                "it.public.invited");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-invite-created",
                Map.of("nx_pl_invite", "invite_id"));

        HttpResponse<String> preview = http.get(PlatformConstant.PUBLIC_INVITES_PATH + "/" + inviteToken);
        assertThat(preview.statusCode()).as(preview.body()).isEqualTo(200);
        assertThat(preview.body()).contains("PENDING");

        String acceptBody = """
                {
                  "loginName": "it.public.invited",
                  "encryptedPassword": "%s"
                }
                """.formatted(PlatformSampleRsaPassword.encrypt("Invited-Pass-1!"));
        HttpResponse<String> accepted = http.postJson(
                PlatformConstant.PUBLIC_INVITES_PATH + "/" + inviteToken + "/accept",
                acceptBody);
        assertThat(accepted.statusCode()).as(accepted.body()).isEqualTo(200);

        Long users = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_pl_user WHERE login_name = ? AND status = 'ACTIVE'",
                Long.class,
                "it.public.invited");
        assertThat(users).isEqualTo(1L);

        String inviteStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM nx_pl_invite WHERE invite_token = ?",
                String.class,
                inviteToken);
        assertThat(inviteStatus).isEqualTo("ACCEPTED");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-accept",
                Map.of("nx_pl_invite", "invite_id", "nx_pl_user", "login_name"));
    }
}
