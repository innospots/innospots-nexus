package com.innospots.nexus.sample.spring.platform.logger;

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
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_AUDIT_LOG_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformAuditLogEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-audit-log";

    private static final String SEED_AUDIT_LOG_ID = "it-audit-log-seed-00000001";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanAuditLogs(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of("nx_audit_log", "audit_log_id"));
    }

    @Test
    void auditLogEndpointsPageAndGetSeededPlatformRows() throws Exception {
        PlatformIntegrationTestSupport.seedAuditLog(jdbcTemplate, SEED_AUDIT_LOG_ID);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-seeded",
                Map.of("nx_audit_log", "audit_log_id"));

        HttpResponse<String> paged = http.get(PlatformConstant.AUDIT_LOGS_PATH + "?pageNo=1&pageSize=20");
        assertThat(paged.statusCode()).as(paged.body()).isEqualTo(200);
        assertThat(paged.body()).contains(SEED_AUDIT_LOG_ID);
        assertThat(paged.body()).contains("platform.test");

        HttpResponse<String> detail = http.get(PlatformConstant.AUDIT_LOGS_PATH + "/" + SEED_AUDIT_LOG_ID);
        assertThat(detail.statusCode()).as(detail.body()).isEqualTo(200);
        assertThat(detail.body()).contains("seeded for IT");

        Long platformRows = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM nx_audit_log
                WHERE audit_log_id = ? AND owner_type = 'PLATFORM' AND security_realm = 'PLATFORM'
                """,
                Long.class,
                SEED_AUDIT_LOG_ID);
        assertThat(platformRows).isEqualTo(1L);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-http",
                Map.of("nx_audit_log", "audit_log_id"));
    }
}
