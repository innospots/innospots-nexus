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
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_PUBLIC_OPEN_REGISTRATION_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT,
        "nexus.console.registration.mode=OPEN",
        "nexus.console.registration.otp-verification=EMAIL",
        "nexus.console.registration.otp.debug-log-enabled=true"
})
class PlatformPublicOpenRegistrationEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-public-open-registration";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    private PlatformOtpTestCapture otpCapture;

    @BeforeEach
    void setUp() throws Exception {
        PlatformIntegrationTestSupport.cleanRegistrationSettings(jdbcTemplate);
        PlatformIntegrationTestSupport.cleanPlatformUsers(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        otpCapture = new PlatformOtpTestCapture();
        HttpResponse<String> mode = http.putJson(
                PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH,
                "{\"registrationMode\":\"OPEN\"}");
        assertThat(mode.statusCode()).as(mode.body()).isEqualTo(200);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-open-mode",
                Map.of("nx_system_setting", "setting_key", "nx_pl_user", "login_name"));
    }

    @AfterEach
    void tearDown() {
        if (otpCapture != null) {
            otpCapture.close();
        }
    }

    @Test
    void openRegistrationOtpAndSubmitCreatesActiveUser() throws Exception {
        String otpBody = """
                {
                  "email": "open.reg@example.com"
                }
                """;
        HttpResponse<String> otp = http.postJson(PlatformConstant.PUBLIC_OPEN_REGISTRATION_PATH + "/otp", otpBody);
        assertThat(otp.statusCode()).as(otp.body()).isEqualTo(200);
        String code = otpCapture.requirePlaintextCode(OtpPurpose.PLATFORM_OPEN_REGISTRATION);

        String registerBody = """
                {
                  "loginName": "it.open.reg",
                  "displayName": "IT Open Reg",
                  "email": "open.reg@example.com",
                  "encryptedPassword": "%s",
                  "verificationCode": "%s"
                }
                """.formatted(PlatformSampleRsaPassword.encrypt("Open-Reg-Pass-1!"), code);
        HttpResponse<String> registered = http.postJson(PlatformConstant.PUBLIC_OPEN_REGISTRATION_PATH, registerBody);
        assertThat(registered.statusCode()).as(registered.body()).isEqualTo(200);
        assertThat(registered.body()).contains("\"success\":true");

        Long activeUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_pl_user WHERE login_name = ? AND status = 'ACTIVE'",
                Long.class,
                "it.open.reg");
        assertThat(activeUsers).isEqualTo(1L);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-after-register",
                Map.of("nx_pl_user", "login_name", "nx_user_credential", "subject_id"));
    }
}
