package com.innospots.nexus.sample.spring.platform.auth;

import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.password.VerificationType;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.support.PlatformDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;
import com.innospots.nexus.sample.spring.platform.support.PlatformOtpTestCapture;
import com.innospots.nexus.sample.spring.platform.support.PlatformSampleRsaPassword;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_PUBLIC_PASSWORD_RESET_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformPublicPasswordResetEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-public-password-reset";

    private static final String USER_ID = "it-reset-user-000000000001";

    private static final String EMAIL = "reset.user@example.com";

    private static final String OLD_PASSWORD = "Reset-Old-Pass-1!";

    private static final String NEW_PASSWORD = "Reset-New-Pass-2!";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private OtpChallengeService otpChallengeService;

    private PlatformEndpointHttpClient http;

    private PlatformOtpTestCapture otpCapture;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanPlatformUsers(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        otpCapture = new PlatformOtpTestCapture();
        jdbcTemplate.update(
                """
                INSERT INTO nx_pl_user (
                    platform_user_id, login_name, display_name, email, status, created_at, updated_at
                ) VALUES (?, 'it.reset.user', 'IT Reset User', ?, 'ACTIVE',
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                USER_ID,
                EMAIL);
        credentialService.enrollPassword(SecurityRealm.PLATFORM, USER_ID, OLD_PASSWORD);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-user-seeded",
                Map.of("nx_pl_user", "login_name", "nx_user_credential", "subject_id"));
    }

    @AfterEach
    void tearDown() {
        if (otpCapture != null) {
            otpCapture.close();
        }
    }

    @Test
    void publicPasswordResetWithOtpUpdatesCredential() throws Exception {
        otpChallengeService.sendPasswordResetCode(SecurityRealm.PLATFORM, EMAIL, VerificationType.EMAIL, null);
        String code = otpCapture.requirePlaintextCode(OtpPurpose.PASSWORD_RESET);

        String resetBody = """
                {
                  "identity": "%s",
                  "verificationCode": "%s",
                  "type": "EMAIL",
                  "newEncryptedPassword": "%s"
                }
                """.formatted(EMAIL, code, PlatformSampleRsaPassword.encrypt(NEW_PASSWORD));
        HttpResponse<String> reset = http.postJson(
                PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH + "/reset",
                resetBody);
        assertThat(reset.statusCode()).as(reset.body()).isEqualTo(200);

        String verifier = jdbcTemplate.queryForObject(
                """
                SELECT verifier FROM nx_user_credential
                WHERE subject_id = ? AND security_realm = 'PLATFORM' AND credential_kind = 'PASSWORD'
                """,
                String.class,
                USER_ID);
        assertThat(CryptoUtils.matchesPassword(NEW_PASSWORD, verifier)).isTrue();
        assertThat(CryptoUtils.matchesPassword(OLD_PASSWORD, verifier)).isFalse();
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-after-reset",
                Map.of("nx_user_credential", "subject_id"));
    }
}
