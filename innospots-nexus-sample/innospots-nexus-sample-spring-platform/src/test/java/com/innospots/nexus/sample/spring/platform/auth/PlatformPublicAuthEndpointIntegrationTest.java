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
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_PUBLIC_AUTH_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformPublicAuthEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-public-auth";

    private static final String LOGIN_NAME = "it.public.login";

    private static final String RAW_PASSWORD = "Public-Login-Pass-1!";

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
        http = new PlatformEndpointHttpClient(port);
        seedLoginUser();
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-user-seeded",
                Map.of("nx_pl_user", "login_name", "nx_user_credential", "subject_id"));
    }

    @Test
    void publicLoginAndRefreshIssueTokens() throws Exception {
        String loginBody = """
                {
                  "login": "%s",
                  "encryptedPassword": "%s"
                }
                """.formatted(LOGIN_NAME, PlatformSampleRsaPassword.encrypt(RAW_PASSWORD));

        HttpResponse<String> login = http.postJson(PlatformConstant.PUBLIC_AUTH_PATH + "/login", loginBody);
        assertThat(login.statusCode()).as(login.body()).isEqualTo(200);
        assertThat(login.body()).contains("\"accessToken\"");
        assertThat(login.body()).contains("\"refreshToken\"");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-after-login",
                Map.of("nx_pl_user", "login_name"));

        String refreshToken = extractJsonStringField(login.body(), "refreshToken");
        String refreshBody = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);
        HttpResponse<String> refreshed = http.postJson(PlatformConstant.AUTH_REFRESH_PATH, refreshBody);
        assertThat(refreshed.statusCode()).as(refreshed.body()).isEqualTo(200);
        assertThat(refreshed.body()).contains("\"accessToken\"");
        assertThat(refreshed.body()).contains("\"refreshToken\"");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-refresh",
                Map.of("nx_pl_user", "login_name"));
    }

    private void seedLoginUser() {
        jdbcTemplate.update(
                """
                INSERT INTO nx_pl_user (
                    platform_user_id, login_name, display_name, status, created_at, updated_at
                ) VALUES ('it-public-login-user-01', ?, 'IT Public Login', 'ACTIVE',
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                LOGIN_NAME);
        credentialService.enrollPassword(
                com.innospots.nexus.console.auth.domain.enums.SecurityRealm.PLATFORM,
                "it-public-login-user-01",
                RAW_PASSWORD);
    }

    private static String extractJsonStringField(String json, String fieldName) {
        String marker = "\"" + fieldName + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Field not found: " + fieldName);
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            throw new IllegalArgumentException("Malformed JSON for field: " + fieldName);
        }
        return json.substring(start, end);
    }
}
