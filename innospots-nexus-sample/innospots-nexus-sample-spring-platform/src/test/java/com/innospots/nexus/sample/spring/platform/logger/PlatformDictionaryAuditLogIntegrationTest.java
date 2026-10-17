package com.innospots.nexus.sample.spring.platform.logger;

import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.sample.spring.platform.support.PlatformDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 字典类型写操作经 {@code ConsoleAuditLogFilter} 落库 {@code nx_audit_log} 的端到端验证（H2，无 Mock）。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_DICTIONARY_AUDIT_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformDictionaryAuditLogIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-dictionary-audit-log";

    private static final String TYPE_CODE = "audit_log_region";

    private static final Map<String, Set<String>> EXPECTED_MESSAGES = Map.of(
            "dictionary.type.create", Set.of("创建字典类型", "Create dictionary type"),
            "dictionary.type.update", Set.of("更新字典类型", "Update dictionary type"),
            "dictionary.type.status.update", Set.of("更新字典类型状态", "Update dictionary type status"),
            "dictionary.type.delete", Set.of("删除字典类型", "Delete dictionary type"));

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanDictionary(jdbcTemplate);
        PlatformIntegrationTestSupport.cleanAuditLogs(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of("nx_audit_log", "audit_log_id", "nx_dictionary_type", "type_code"));
    }

    @Test
    void dictionaryMutationsPersistAuditLogsWithExpectedFields() throws Exception {
        String createBody = """
                {
                  "typeCode": "audit_log_region",
                  "typeName": {
                    "zh-CN": "审计测试区域",
                    "en": "Audit Region"
                  }
                }
                """;
        HttpResponse<String> created = http.postJson(PlatformConstant.DICTIONARY_TYPES_PATH, createBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        assertThat(created.headers().firstValue("X-Request-Id"))
                .as("Jersey ConsoleRequestContextFilter 应在响应阶段写入 X-Request-Id")
                .isPresent();

        String typeId = jdbcTemplate.queryForObject(
                "SELECT dictionary_type_id FROM nx_dictionary_type WHERE type_code = ?",
                String.class,
                TYPE_CODE);
        assertThat(typeId).isNotBlank();

        HttpResponse<String> readOnly = http.get(PlatformConstant.DICTIONARY_TYPES_PATH + "/" + typeId);
        assertThat(readOnly.statusCode()).as(readOnly.body()).isEqualTo(200);
        assertThat(countDictionaryAuditRows()).isEqualTo(1L);
        assertAuditRow("dictionary.type.create", "POST", "dictionary-types", null);

        String updateBody = """
                {
                  "typeName": {
                    "en": "Audit Region Updated"
                  }
                }
                """;
        HttpResponse<String> updated = http.putJson(
                PlatformConstant.DICTIONARY_TYPES_PATH + "/" + typeId,
                updateBody);
        assertThat(updated.statusCode()).as(updated.body()).isEqualTo(200);
        assertThat(countDictionaryAuditRows()).isEqualTo(2L);
        assertAuditRow("dictionary.type.update", "PUT", "dictionary-types", typeId);

        HttpResponse<String> disabled = http.putJson(
                PlatformConstant.DICTIONARY_TYPES_PATH + "/" + typeId + "/status",
                "{\"status\":\"DISABLED\"}");
        assertThat(disabled.statusCode()).as(disabled.body()).isEqualTo(200);
        assertThat(countDictionaryAuditRows()).isEqualTo(3L);
        assertAuditRow("dictionary.type.status.update", "PUT", "dictionary-types", typeId);

        HttpResponse<String> deleted = http.delete(PlatformConstant.DICTIONARY_TYPES_PATH + "/" + typeId);
        assertThat(deleted.statusCode()).as(deleted.body()).isEqualTo(200);
        assertThat(countDictionaryAuditRows()).isEqualTo(4L);
        assertAuditRow("dictionary.type.delete", "DELETE", "dictionary-types", typeId);

        String auditLogJson = dumpAuditLogTableJson();
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-audit-rows-json",
                Map.of("nx_audit_log", "operated_time"));

        HttpResponse<String> auditPage = http.get(PlatformConstant.AUDIT_LOGS_PATH + "?pageNo=1&pageSize=20");
        assertThat(auditPage.statusCode()).as(auditPage.body()).isEqualTo(200);
        assertThat(auditPage.body()).contains("dictionary.type.create");
        assertThat(auditPage.body()).contains("dictionary.type.update");
        assertThat(auditPage.body()).contains("dictionary.type.delete");
        assertThat(auditPage.body()).contains("dictionary.type.status.update");
        assertThat(Jsons.mapper().readTree(auditPage.body()).path("data").path("records").size()).isEqualTo(4);
        assertThat(countDictionaryAuditRows()).isEqualTo(4L);

        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "03-after-http-query",
                Map.of("nx_audit_log", "operated_time"));

        assertThat(auditLogJson)
                .as("nx_audit_log 全表 JSON（集成测试落盘见 target/test-output/%s/）", OUTPUT_DIR)
                .contains("dictionary.type.create")
                .contains("dictionary.type.update")
                .contains("dictionary.type.delete");
    }

    @Test
    void failedMutationPersistsFailureAuditLogWithoutChangingDictionary() throws Exception {
        HttpResponse<String> failed = http.delete(PlatformConstant.DICTIONARY_TYPES_PATH + "/missing-type");

        assertThat(failed.statusCode()).as(failed.body()).isEqualTo(404);
        assertThat(failed.headers().firstValue("X-Request-Id")).isPresent();
        assertThat(countDictionaryAuditRows()).isEqualTo(1L);
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM nx_audit_log");
        assertThat(row.get("action")).isEqualTo("dictionary.type.delete");
        assertThat(row.get("execution_result")).isEqualTo("FAILURE");
        assertThat(row.get("owner_type")).isEqualTo("PLATFORM");
        assertThat(row.get("owner_id")).isNull();
        assertThat(row.get("security_realm")).isEqualTo("PLATFORM");
        assertThat(row.get("actor")).isEqualTo(PlatformIntegrationTestSupport.DEV_PLATFORM_USER_ID);
        assertThat(row.get("path")).isEqualTo("DELETE " + PlatformConstant.DICTIONARY_TYPES_PATH + "/missing-type");
        assertThat(row.get("message").toString()).contains("NexusException");
        assertThat(((Number) row.get("duration_ms")).longValue()).isGreaterThanOrEqualTo(0L);
        assertThat(row.get("key_parameters")).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nx_dictionary_type", Long.class)).isZero();

        HttpResponse<String> auditPage = http.get(PlatformConstant.AUDIT_LOGS_PATH + "?pageNo=1&pageSize=20");
        assertThat(auditPage.statusCode()).as(auditPage.body()).isEqualTo(200);
        assertThat(auditPage.body()).contains("dictionary.type.delete", "FAILURE");
        assertThat(countDictionaryAuditRows()).isEqualTo(1L);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate, OUTPUT_DIR, "04-failed-mutation", Map.of("nx_audit_log", "operated_time"));
    }

    private String dumpAuditLogTableJson() {
        return Jsons.toJson(Map.of(
                "nx_audit_log",
                jdbcTemplate.queryForList("SELECT * FROM nx_audit_log ORDER BY operated_time, action")));
    }

    private long countDictionaryAuditRows() {
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM nx_audit_log
                WHERE action LIKE 'dictionary.type.%'
                """,
                Long.class);
        return count == null ? 0L : count;
    }

    private void assertAuditRow(
            String action,
            String httpMethod,
            String pathSegment,
            String typeIdInPath
    ) {
        List<AuditLogRow> rows = jdbcTemplate.query(
                """
                SELECT audit_log_id, action, path, message, execution_result, actor,
                       owner_type, owner_id, security_realm, duration_ms, key_parameters, operated_time
                FROM nx_audit_log
                WHERE action = ?
                """,
                (rs, rowNum) -> new AuditLogRow(
                        rs.getString("audit_log_id"),
                        rs.getString("action"),
                        rs.getString("path"),
                        rs.getString("message"),
                        rs.getString("execution_result"),
                        rs.getString("actor"),
                        rs.getString("owner_type"),
                        rs.getString("owner_id"),
                        rs.getString("security_realm"),
                        rs.getObject("duration_ms") == null ? null : rs.getLong("duration_ms"),
                        rs.getString("key_parameters"),
                        rs.getTimestamp("operated_time")),
                action);
        assertThat(rows).hasSize(1);
        AuditLogRow row = rows.get(0);

        assertThat(row.auditLogId()).isNotBlank();
        assertThat(row.action()).isEqualTo(action);
        assertThat(EXPECTED_MESSAGES.get(action)).contains(row.message());
        assertThat(row.executionResult()).isEqualTo("SUCCESS");
        assertThat(row.actor()).isEqualTo(PlatformIntegrationTestSupport.DEV_PLATFORM_USER_ID);
        assertThat(row.securityRealm()).isEqualTo("PLATFORM");
        assertThat(row.ownerType()).isEqualTo("PLATFORM");
        assertThat(row.ownerId()).isNull();
        assertThat(row.keyParameters()).isNull();
        assertThat(row.durationMs()).isNotNull();
        assertThat(row.durationMs()).isGreaterThanOrEqualTo(0L);
        assertThat(row.operatedTime()).isNotNull();

        assertThat(row.path()).startsWith(httpMethod + " ");
        assertThat(row.path()).contains(pathSegment);
        if (typeIdInPath != null) {
            assertThat(row.path()).contains(typeIdInPath);
        }
    }

    private record AuditLogRow(
            String auditLogId,
            String action,
            String path,
            String message,
            String executionResult,
            String actor,
            String ownerType,
            String ownerId,
            String securityRealm,
            Long durationMs,
            String keyParameters,
            Timestamp operatedTime
    ) {
    }
}
