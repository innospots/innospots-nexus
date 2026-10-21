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
import com.innospots.nexus.console.role.endpoint.RoleEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.role.endpoint.PlatformRoleEndpoint;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.role.support.RoleDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 运营平台角色 REST 集成测试：路径 {@link PlatformConstant#ROLES_PATH}，会话为 console-web.yaml
 * {@code dev-session}（PLATFORM）；创建请求不含 ownerType。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_ROLE_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class RoleEndpointCrudIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(RoleEndpointCrudIntegrationTest.class);

    private static final String ROLES_BASE = PlatformConstant.ROLES_PATH;

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleEndpoint roleEndpoint;

    @Autowired
    private PlatformRoleEndpoint platformRoleEndpoint;

    private RoleEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM nx_role_binding");
        jdbcTemplate.execute("DELETE FROM nx_role");
        http = new RoleEndpointHttpClient(port);
        assertThat(roleEndpoint).isSameAs(platformRoleEndpoint);
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "00-empty");
    }

    @Test
    void createAndQueryRoleUnderPlatformDevSession() throws Exception {
        String createBody = """
                {
                  "roleName": "Platform Operators",
                  "roleCode": "platform-operators",
                  "description": "sample platform integration"
                }
                """;

        HttpResponse<String> created = http.postJson(ROLES_BASE, createBody);
        LOGGER.info("POST {} status={} body={}", ROLES_BASE, created.statusCode(), created.body());
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        assertThat(created.body()).contains("\"success\":true");
        assertThat(created.body()).contains("\"roleCode\":\"platform-operators\"");
        assertThat(created.body()).contains("\"ownerType\":\"PLATFORM\"");
        assertThat(created.body()).contains("\"securityRealm\":\"PLATFORM\"");
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "01-after-create");

        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT role_id, role_name, role_code, owner_type, owner_id, security_realm, status,
                       sort_order, built_in, administrator, created_at, updated_at
                FROM nx_role WHERE role_code = ?
                """,
                "platform-operators");
        String roleId = String.valueOf(row.get("role_id"));
        assertThat(roleId).isNotBlank();
        assertThat(row.get("role_name")).isEqualTo("Platform Operators");
        assertThat(row.get("owner_type")).isEqualTo("PLATFORM");
        assertThat(row.get("owner_id")).isNull();
        assertThat(row.get("security_realm")).isEqualTo("PLATFORM");
        assertThat(row.get("status")).isEqualTo("ENABLED");
        assertThat(row.get("sort_order")).isEqualTo(1);
        assertThat(row.get("built_in")).isEqualTo(false);
        assertThat(row.get("administrator")).isEqualTo(false);
        assertThat(row.get("created_at")).isNotNull();
        assertThat(row.get("updated_at")).isNotNull();
        LOGGER.info("nx_role row after create: {}", Jsons.toJson(row));

        HttpResponse<String> detail = http.get(ROLES_BASE + "/" + roleId);
        LOGGER.info("GET detail status={} body={}", detail.statusCode(), detail.body());
        assertThat(detail.statusCode()).as(detail.body()).isEqualTo(200);
        assertThat(detail.body()).contains("Platform Operators");

        String pageQuery = ROLES_BASE
                + "?pageNo=1&pageSize=20&input="
                + URLEncoder.encode("platform-operators", StandardCharsets.UTF_8);
        HttpResponse<String> page = http.get(pageQuery);
        LOGGER.info("GET page status={} body={}", page.statusCode(), page.body());
        assertThat(page.statusCode()).as(page.body()).isEqualTo(200);
        assertThat(page.body()).contains(roleId);

        HttpResponse<String> options = http.get(ROLES_BASE + "/options?status=ENABLED");
        LOGGER.info("GET options status={} body={}", options.statusCode(), options.body());
        assertThat(options.statusCode()).as(options.body()).isEqualTo(200);
        assertThat(options.body()).contains(roleId);

        String updateBody = """
                {
                  "roleName": "Platform Operators Updated",
                  "description": "updated in sample IT",
                  "sortOrder": 20
                }
                """;
        HttpResponse<String> updated = http.exchange(
                "PUT", ROLES_BASE + "/" + roleId, Map.of("Content-Type", "application/json"), updateBody);
        assertThat(updated.statusCode()).as(updated.body()).isEqualTo(200);
        assertThat(updated.body()).contains("Platform Operators Updated");
        Map<String, Object> updatedRow = jdbcTemplate.queryForMap(
                """
                SELECT role_name, description, sort_order, status, created_at, updated_at
                FROM nx_role WHERE role_id = ?
                """,
                roleId);
        assertThat(updatedRow.get("role_name")).isEqualTo("Platform Operators Updated");
        assertThat(updatedRow.get("description")).isEqualTo("updated in sample IT");
        assertThat(updatedRow.get("sort_order")).isEqualTo(20);
        assertThat(updatedRow.get("status")).isEqualTo("ENABLED");
        assertThat(updatedRow.get("created_at")).isNotNull();
        assertThat(updatedRow.get("updated_at")).isNotNull();
        LOGGER.info("nx_role row after update: {}", Jsons.toJson(updatedRow));
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "02-after-update");

        HttpResponse<String> disabled = http.exchange(
                "PUT",
                ROLES_BASE + "/" + roleId + "/status",
                Map.of("Content-Type", "application/json"),
                "{\"status\":\"DISABLED\"}");
        assertThat(disabled.statusCode()).as(disabled.body()).isEqualTo(200);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM nx_role WHERE role_id = ?", String.class, roleId))
                .isEqualTo("DISABLED");
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "03-after-disable");

        HttpResponse<String> pageAfterDisable = http.get(ROLES_BASE + "?pageNo=1&pageSize=20&status=DISABLED");
        assertThat(pageAfterDisable.statusCode()).as(pageAfterDisable.body()).isEqualTo(200);
        assertThat(pageAfterDisable.body()).contains(roleId);

        HttpResponse<String> deleted = http.exchange(
                "DELETE", ROLES_BASE + "/" + roleId, Map.of(), null);
        assertThat(deleted.statusCode()).as(deleted.body()).isEqualTo(200);
        RoleDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "04-after-delete");

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nx_role", Long.class);
        assertThat(count).isZero();
        LOGGER.info("final nx_role rows: {}", Jsons.toJson(
                jdbcTemplate.queryForList("SELECT * FROM nx_role ORDER BY role_code")));
    }

    @Test
    void createRequestIgnoresClientOwnerTypeAndStaysPlatform() throws Exception {
        String createBody = """
                {
                  "roleName": "Ignored Owner Type",
                  "roleCode": "ignored-owner-type",
                  "ownerType": "TENANT",
                  "ownerId": "dev-tenant",
                  "description": "client must not choose ownership"
                }
                """;
        LOGGER.info("create request with stray owner fields={}", createBody.replaceAll("\\s+", " ").trim());

        HttpResponse<String> created = http.postJson(ROLES_BASE, createBody);
        LOGGER.info("POST status={} body={}", created.statusCode(), created.body());
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        assertThat(created.body()).contains("\"ownerType\":\"PLATFORM\"");
        assertThat(created.body()).contains("\"securityRealm\":\"PLATFORM\"");
        assertThat(created.body()).contains("\"ownerId\":null");

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT owner_type, owner_id, security_realm FROM nx_role WHERE role_code = ?",
                "ignored-owner-type");
        assertThat(row.get("owner_type")).isEqualTo("PLATFORM");
        assertThat(row.get("owner_id")).isNull();
        assertThat(row.get("security_realm")).isEqualTo("PLATFORM");

        HttpResponse<String> page = http.get(ROLES_BASE + "?pageNo=1&pageSize=20");
        LOGGER.info("GET page status={} body={}", page.statusCode(), page.body());
        assertThat(page.body()).contains("ignored-owner-type");
        assertThat(page.body()).doesNotContain("\"ownerType\":\"TENANT\"");
    }
}
