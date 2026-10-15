package com.innospots.nexus.sample.spring.platform.access;

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

import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
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
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_PUBLIC_ACCESS_REGISTRATION_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformPublicAccessRegistrationEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-public-access-registration";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    private PlatformOtpTestCapture otpCapture;

    @BeforeEach
    void setUp() throws Exception {
        PlatformIntegrationTestSupport.cleanRegistrationSettings(jdbcTemplate);
        PlatformIntegrationTestSupport.cleanAccessRequests(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        otpCapture = new PlatformOtpTestCapture();
        HttpResponse<String> mode = http.putJson(
                PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH,
                "{\"registrationMode\":\"APPROVAL\"}");
        assertThat(mode.statusCode()).as(mode.body()).isEqualTo(200);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-approval-mode",
                Map.of(
                        "nx_pl_access_request",
                        "access_request_id",
                        "nx_pl_user",
                        "login_name"));
    }

    @AfterEach
    void tearDown() {
        if (otpCapture != null) {
            otpCapture.close();
        }
    }

    @Test
    void accessRegistrationSubmitCreatesPendingUserAndRequest() throws Exception {
        String otpBody = """
                {
                  "email": "access.reg@example.com"
                }
                """;
        HttpResponse<String> otp = http.postJson(
                PlatformConstant.PUBLIC_ACCESS_REGISTRATION_PATH + "/otp",
                otpBody);
        assertThat(otp.statusCode()).as(otp.body()).isEqualTo(200);
        String code = otpCapture.requirePlaintextCode(OtpPurpose.PLATFORM_ACCESS_VERIFY);

        String submitBody = """
                {
                  "loginName": "it.access.reg",
                  "displayName": "IT Access Reg",
                  "email": "access.reg@example.com",
                  "description": "Need platform access",
                  "encryptedPassword": "%s",
                  "verificationCode": "%s"
                }
                """.formatted(PlatformSampleRsaPassword.encrypt("Access-Reg-Pass-1!"), code);
        HttpResponse<String> submitted = http.postJson(
                PlatformConstant.PUBLIC_ACCESS_REGISTRATION_PATH,
                submitBody);
        assertThat(submitted.statusCode()).as(submitted.body()).isEqualTo(200);
        assertThat(submitted.body()).contains("PENDING_APPROVAL");

        Long pendingUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_pl_user WHERE login_name = ? AND status = 'PENDING_APPROVAL'",
                Long.class,
                "it.access.reg");
        assertThat(pendingUsers).isEqualTo(1L);

        Long requests = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM nx_pl_access_request
                WHERE login_name = ? AND status = 'PENDING_APPROVAL'
                """,
                Long.class,
                "it.access.reg");
        assertThat(requests).isEqualTo(1L);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-after-submit",
                Map.of("nx_pl_access_request", "access_request_id", "nx_pl_user", "login_name"));
    }
}
