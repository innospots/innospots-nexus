package com.innospots.nexus.sample.spring.platform.settings;

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
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_SETTINGS_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformSettingsEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-settings";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanRegistrationSettings(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of("nx_system_setting", "setting_key"));
    }

    @Test
    void registrationModeSettingGetAndPutPersistSystemSetting() throws Exception {
        HttpResponse<String> initial = http.get(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH);
        assertThat(initial.statusCode()).as(initial.body()).isEqualTo(200);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-initial-get",
                Map.of("nx_system_setting", "setting_key"));

        String updateBody = """
                {
                  "registrationMode": "APPROVAL"
                }
                """;
        HttpResponse<String> updated = http.putJson(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH, updateBody);
        assertThat(updated.statusCode()).as(updated.body()).isEqualTo(200);
        assertThat(updated.body()).contains("APPROVAL");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-put",
                Map.of("nx_system_setting", "setting_key"));

        String stored = jdbcTemplate.queryForObject(
                """
                SELECT setting_value FROM nx_system_setting
                WHERE setting_domain = 'platform' AND setting_scope = 'GLOBAL'
                  AND scope_id = '' AND setting_key = 'registration.mode'
                """,
                String.class);
        assertThat(stored).isEqualTo("APPROVAL");

        HttpResponse<String> afterGet = http.get(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH);
        assertThat(afterGet.statusCode()).isEqualTo(200);
        assertThat(afterGet.body()).contains("APPROVAL");
    }
}
