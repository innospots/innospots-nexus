package com.innospots.nexus.sample.spring.platform.access;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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

/**
 * 公开主动注册（APPROVAL）→ 管理端查询 → 审批通过 → 平台用户可查，全链路 HTTP E2E。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_ACCESS_APPROVAL_E2E_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformAccessRegistrationApprovalE2EIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-access-approval-e2e";

    private static final String LOGIN_NAME = "it.e2e.access.reg";

    private static final String RAW_PASSWORD = "E2e-Access-Reg-Pass-1!";

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
    }

    @AfterEach
    void tearDown() {
        if (otpCapture != null) {
            otpCapture.close();
        }
    }

    @Test
    void publicSubmitAdminApproveAndQueryActivePlatformUser() throws Exception {
        String otpBody = """
                {
                  "email": "e2e.access.reg@example.com"
                }
                """;
        HttpResponse<String> otp = http.postJson(
                PlatformConstant.PUBLIC_ACCESS_REGISTRATION_PATH + "/otp",
                otpBody);
        assertThat(otp.statusCode()).as(otp.body()).isEqualTo(200);
        String code = otpCapture.requirePlaintextCode(OtpPurpose.PLATFORM_ACCESS_VERIFY);

        String submitBody = """
                {
                  "loginName": "%s",
                  "displayName": "E2E Access Reg",
                  "email": "e2e.access.reg@example.com",
                  "description": "E2E approval flow",
                  "encryptedPassword": "%s",
                  "verificationCode": "%s"
                }
                """.formatted(LOGIN_NAME, PlatformSampleRsaPassword.encrypt(RAW_PASSWORD), code);
        HttpResponse<String> submitted = http.postJson(
                PlatformConstant.PUBLIC_ACCESS_REGISTRATION_PATH,
                submitBody);
        assertThat(submitted.statusCode()).as(submitted.body()).isEqualTo(200);
        assertThat(submitted.body()).contains("PENDING_APPROVAL");
        String accessRequestId = extractJsonStringField(submitted.body(), "accessRequestId");
        String pendingUserId = extractJsonStringField(submitted.body(), "platformUserId");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-after-public-submit",
                Map.of("nx_pl_access_request", "access_request_id", "nx_pl_user", "login_name"));

        String adminListPath = PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH
                + "?pageNo=1&pageSize=20&input="
                + URLEncoder.encode(LOGIN_NAME, StandardCharsets.UTF_8);
        HttpResponse<String> paged = http.get(adminListPath);
        assertThat(paged.statusCode()).as(paged.body()).isEqualTo(200);
        assertThat(paged.body()).contains(accessRequestId);

        HttpResponse<String> detail = http.get(
                PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH + "/" + accessRequestId);
        assertThat(detail.statusCode()).as(detail.body()).isEqualTo(200);
        assertThat(detail.body()).contains(LOGIN_NAME);
        assertThat(detail.body()).contains(pendingUserId);

        HttpResponse<String> approved = http.postJson(
                PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH + "/" + accessRequestId + "/approve",
                "{}");
        assertThat(approved.statusCode()).as(approved.body()).isEqualTo(200);
        assertThat(approved.body()).contains("ACTIVE");

        HttpResponse<String> user = http.get(PlatformConstant.USERS_PATH + "/" + pendingUserId);
        assertThat(user.statusCode()).as(user.body()).isEqualTo(200);
        assertThat(user.body()).contains(LOGIN_NAME);
        assertThat(user.body()).contains("ACTIVE");

        String requestStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM nx_pl_access_request WHERE access_request_id = ?",
                String.class,
                accessRequestId);
        assertThat(requestStatus).isEqualTo("APPROVED");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-approve-and-user-get",
                Map.of("nx_pl_access_request", "access_request_id", "nx_pl_user", "login_name"));

        String loginBody = """
                {
                  "login": "%s",
                  "encryptedPassword": "%s"
                }
                """.formatted(LOGIN_NAME, PlatformSampleRsaPassword.encrypt(RAW_PASSWORD));
        HttpResponse<String> login = http.postJson(PlatformConstant.PUBLIC_AUTH_PATH + "/login", loginBody);
        assertThat(login.statusCode()).as(login.body()).isEqualTo(200);
        assertThat(login.body()).contains("\"accessToken\"");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "03-after-public-login",
                Map.of("nx_pl_user", "login_name"));
    }

    private static String extractJsonStringField(String json, String fieldName) {
        String marker = "\"" + fieldName + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Field not found: " + fieldName + " in " + json);
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            throw new IllegalArgumentException("Malformed JSON for field: " + fieldName);
        }
        return json.substring(start, end);
    }
}
