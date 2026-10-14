package com.innospots.nexus.sample.spring.platform.role;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.role.endpoint.RoleEndpoint;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.role.support.RoleDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 运营平台示例应用上的角色 REST 集成测试：Jersey HTTP + H2（{@code test} profile）校验 CRUD 与库表数据。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:nexus-sample-platform-role-it;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.config.import=optional:classpath:nexus/console-auth.yaml,optional:classpath:nexus/console-credential.yaml,optional:classpath:nexus/console-web.yaml,optional:classpath:nexus/console-openapi.yaml,optional:classpath:nexus/console-plugins.yaml"
})
class RoleEndpointCrudIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(RoleEndpointCrudIntegrationTest.class);

    /** 与 {@code nexus/console-web.yaml} dev-session.workspace-id 一致 */
    private static final String WORKSPACE_ID = "dev-workspace";

    private static final String ROLES_BASE = ConsoleConstant.apiPath("/roles");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleEndpoint roleEndpoint;

    private RoleEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM nx_role_binding");
        jdbcTemplate.execute("DELETE FROM nx_role");
        http = new RoleEndpointHttpClient(port);
        assertThat(roleEndpoint).isNotNull();
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "00-empty");
    }

    @Test
    void httpRoleCrudPersistsExpectedDatabaseRows() throws Exception {
        String createBody = """
                {
                  "roleName": "Platform Operators",
                  "roleCode": "platform-operators",
                  "ownerType": "WORKSPACE",
                  "ownerId": "%s",
                  "securityRealm": "TENANT",
                  "description": "sample platform integration",
                  "sortOrder": 10
                }
                """.formatted(WORKSPACE_ID);

        HttpResponse<String> created = http.postJson(ROLES_BASE, createBody);
        LOGGER.info("POST {} status={} body={}", ROLES_BASE, created.statusCode(), created.body());
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        assertThat(created.body()).contains("\"success\":true");
        assertThat(created.body()).contains("platform-operators");
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "01-after-create");

        String roleId = jdbcTemplate.queryForObject(
                "SELECT role_id FROM nx_role WHERE role_code = ?",
                String.class,
                "platform-operators");
        assertThat(roleId).isNotBlank();

        HttpResponse<String> detail = http.get(ROLES_BASE + "/" + roleId);
        LOGGER.info("GET {} status={} body={}", ROLES_BASE + "/" + roleId, detail.statusCode(), detail.body());
        assertThat(detail.statusCode()).isEqualTo(200);
        assertThat(detail.body()).contains("Platform Operators");

        String updateBody = """
                {
                  "roleName": "Platform Operators Updated",
                  "description": "updated in sample IT",
                  "sortOrder": 20
                }
                """;
        HttpResponse<String> updated = http.exchange(
                "PUT", ROLES_BASE + "/" + roleId, Map.of("Content-Type", "application/json"), updateBody);
        LOGGER.info("PUT {} status={} body={}", ROLES_BASE + "/" + roleId, updated.statusCode(), updated.body());
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(updated.body()).contains("Platform Operators Updated");
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "02-after-update");

        HttpResponse<String> disabled = http.exchange(
                "PUT",
                ROLES_BASE + "/" + roleId + "/status",
                Map.of("Content-Type", "application/json"),
                "{\"status\":\"DISABLED\"}");
        LOGGER.info("PUT status status={} body={}", disabled.statusCode(), disabled.body());
        assertThat(disabled.statusCode()).isEqualTo(200);
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "03-after-disable");

        HttpResponse<String> page = http.get(ROLES_BASE + "?pageNo=1&pageSize=20");
        LOGGER.info("GET page status={} body={}", page.statusCode(), page.body());
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body()).contains(roleId);
        assertThat(page.body()).contains("DISABLED");

        HttpResponse<String> deleted = http.exchange(
                "DELETE", ROLES_BASE + "/" + roleId, Map.of(), null);
        LOGGER.info("DELETE {} status={} body={}", ROLES_BASE + "/" + roleId, deleted.statusCode(), deleted.body());
        assertThat(deleted.statusCode()).isEqualTo(200);
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "04-after-delete");

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nx_role", Long.class);
        assertThat(count).isZero();

        LOGGER.info("final nx_role rows: {}", Jsons.toJson(
                jdbcTemplate.queryForList("SELECT * FROM nx_role ORDER BY role_code")));
    }
}
