package com.innospots.nexus.spring.console.role;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.role.endpoint.RoleEndpoint;
import com.innospots.nexus.spring.console.role.support.RoleEndpointH2Support;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestApplication;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestProperties;
import com.innospots.nexus.spring.console.role.support.RoleEndpointTestHttpClient;
import com.innospots.nexus.spring.console.role.support.RoleEndpointTestSessions;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 经 Jersey Servlet 对角色 REST 路由的 HTTP 集成测试（CRUD + 库表 JSON 日志）。
 */
@SpringBootTest(
        classes = RoleEndpointIntegrationTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RoleEndpointIntegrationTestProperties
class RoleEndpointCrudHttpIntegrationTest extends RoleEndpointH2Support {

    private static final Logger LOGGER = LogManager.getLogger(RoleEndpointCrudHttpIntegrationTest.class);

    private static final String ROLES_BASE = ConsoleConstant.apiPath("/roles");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleEndpoint roleEndpoint;

    private RoleEndpointTestHttpClient http;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM nx_role_binding");
        jdbcTemplate.execute("DELETE FROM nx_role");
        http = new RoleEndpointTestHttpClient(port);
        assertThat(roleEndpoint).isNotNull();
        logTable("00-empty");
    }

    @Test
    void httpRoleCrudMatchesDatabase() throws Exception {
        String createBody = """
                {
                  "roleName": "HTTP Operators",
                  "roleCode": "http-operators",
                  "ownerType": "WORKSPACE",
                  "ownerId": "%s",
                  "securityRealm": "TENANT",
                  "description": "http integration",
                  "sortOrder": 5
                }
                """.formatted(RoleEndpointTestSessions.WORKSPACE_ID);

        HttpResponse<String> created = http.postJson(ROLES_BASE, createBody);
        LOGGER.info("POST {} status={} body={}", ROLES_BASE, created.statusCode(), created.body());
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        assertThat(created.body()).contains("\"success\":true");
        assertThat(created.body()).contains("http-operators");

        String roleId = jdbcTemplate.queryForObject(
                "SELECT role_id FROM nx_role WHERE role_code = ?",
                String.class,
                "http-operators");
        assertThat(roleId).isNotBlank();
        logTable("01-after-create");

        HttpResponse<String> detail = http.get(ROLES_BASE + "/" + roleId);
        LOGGER.info("GET {} status={} body={}", ROLES_BASE + "/" + roleId, detail.statusCode(), detail.body());
        assertThat(detail.body()).contains("HTTP Operators");

        String updateBody = """
                {
                  "roleName": "HTTP Operators Updated",
                  "description": "http updated",
                  "sortOrder": 15
                }
                """;
        HttpResponse<String> updated = http.exchange(
                "PUT", ROLES_BASE + "/" + roleId, Map.of("Content-Type", "application/json"), updateBody);
        LOGGER.info("PUT {} status={} body={}", ROLES_BASE + "/" + roleId, updated.statusCode(), updated.body());
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(updated.body()).contains("HTTP Operators Updated");
        logTable("02-after-update");

        HttpResponse<String> disabled = http.exchange(
                "PUT",
                ROLES_BASE + "/" + roleId + "/status",
                Map.of("Content-Type", "application/json"),
                "{\"status\":\"DISABLED\"}");
        LOGGER.info("PUT status status={} body={}", disabled.statusCode(), disabled.body());
        assertThat(disabled.statusCode()).isEqualTo(200);
        logTable("03-after-disable");

        HttpResponse<String> deleted = http.exchange(
                "DELETE", ROLES_BASE + "/" + roleId, Map.of(), null);
        LOGGER.info("DELETE {} status={} body={}", ROLES_BASE + "/" + roleId, deleted.statusCode(), deleted.body());
        assertThat(deleted.statusCode()).isEqualTo(200);
        logTable("04-after-delete");

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nx_role", Long.class);
        assertThat(count).isZero();
    }

    private void logTable(String step) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM nx_role ORDER BY role_code");
        LOGGER.info("HTTP role CRUD DB [{}] nx_role={}", step, Jsons.toJson(rows));
    }
}
