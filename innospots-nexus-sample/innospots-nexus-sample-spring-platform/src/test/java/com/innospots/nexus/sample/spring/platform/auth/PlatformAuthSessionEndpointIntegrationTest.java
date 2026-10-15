package com.innospots.nexus.sample.spring.platform.auth;

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

import com.innospots.nexus.console.credential.password.service.CredentialService;
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
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_AUTH_SESSION_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformAuthSessionEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-auth-session";

    private static final String INITIAL_PASSWORD = "DevSession-Pass-1!";

    private static final String UPDATED_PASSWORD = "DevSession-Pass-2!";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CredentialService credentialService;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanPlatformUsers(jdbcTemplate);
        PlatformIntegrationTestSupport.ensureDevSessionPlatformUser(jdbcTemplate, credentialService, INITIAL_PASSWORD);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-dev-user-seeded",
                Map.of("nx_pl_user", "platform_user_id", "nx_user_credential", "subject_id"));
    }

    @Test
    void passwordChangeAndLogoutEndpointsSucceedUnderDevSession() throws Exception {
        String changeBody = """
                {
                  "oldEncryptedPassword": "%s",
                  "newEncryptedPassword": "%s"
                }
                """.formatted(
                PlatformSampleRsaPassword.encrypt(INITIAL_PASSWORD),
                PlatformSampleRsaPassword.encrypt(UPDATED_PASSWORD));

        HttpResponse<String> changed = http.postJson(PlatformConstant.AUTH_PATH + "/password/change", changeBody);
        assertThat(changed.statusCode()).as(changed.body()).isEqualTo(200);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-after-password-change",
                Map.of("nx_user_credential", "subject_id"));

        Integer credentialVersion = jdbcTemplate.queryForObject(
                """
                SELECT credential_version FROM nx_user_credential
                WHERE subject_id = ? AND security_realm = 'PLATFORM'
                """,
                Integer.class,
                PlatformIntegrationTestSupport.DEV_PLATFORM_USER_ID);
        assertThat(credentialVersion).isNotNull();
        assertThat(credentialVersion).isGreaterThanOrEqualTo(1);

        HttpResponse<String> logout = http.postJson(PlatformConstant.AUTH_PATH + "/logout", null);
        assertThat(logout.statusCode()).as(logout.body()).isEqualTo(200);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-logout",
                Map.of("nx_pl_user", "platform_user_id"));
    }
}
