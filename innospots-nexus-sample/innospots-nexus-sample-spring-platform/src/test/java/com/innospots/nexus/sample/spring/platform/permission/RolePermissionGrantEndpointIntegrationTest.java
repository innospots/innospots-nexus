package com.innospots.nexus.sample.spring.platform.permission;

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
import com.innospots.nexus.console.permission.endpoint.GrantManagementEndpoint;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.permission.support.PermissionGrantDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.permission.support.SyncedCatalogResourceIds;
import com.innospots.nexus.sample.spring.platform.permission.support.SyncedCatalogResourceIds.UserMainMenuPageIds;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 角色权限授权 REST 集成测试：使用 H2 内存库、启动同步的真实目录资源与 HTTP 全量替换/查询。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:nexus-sample-platform-permission-it;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.config.import=optional:classpath:nexus/console-auth.yaml,optional:classpath:nexus/console-credential.yaml,optional:classpath:nexus/console-web.yaml,optional:classpath:nexus/console-openapi.yaml,optional:classpath:nexus/console-plugins.yaml"
})
class RolePermissionGrantEndpointIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(RolePermissionGrantEndpointIntegrationTest.class);

    private static final String WORKSPACE_ID = "dev-workspace";

    private static final String ROLES_BASE = ConsoleConstant.apiPath("/roles");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GrantManagementEndpoint grantManagementEndpoint;

    private RoleEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM nx_permission_grant");
        jdbcTemplate.execute("DELETE FROM nx_role_binding");
        jdbcTemplate.execute("DELETE FROM nx_role");
        http = new RoleEndpointHttpClient(port);
        assertThat(grantManagementEndpoint).isNotNull();
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "00-clean");
    }

    @Test
    void replaceAndListRolePermissionsUsingSyncedCatalogResources() throws Exception {
        UserMainMenuPageIds catalog = SyncedCatalogResourceIds.requireUserMainMenuAndPage(jdbcTemplate);
        LOGGER.info("Synced catalog resources for grant test: {}", Jsons.toJson(catalog));

        String roleId = createRole("permission-sample", "Permission Sample Role");
        String permissionsPath = ConsoleConstant.apiPath("/roles/" + roleId + "/permissions");

        HttpResponse<String> emptyList = http.get(permissionsPath);
        LOGGER.info("GET {} status={} body={}", permissionsPath, emptyList.statusCode(), emptyList.body());
        assertThat(emptyList.statusCode()).as(emptyList.body()).isEqualTo(200);
        assertThat(emptyList.body()).contains("\"grants\":[]");
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "01-after-create-role");

        String replaceBody = """
                {
                  "grants": [
                    { "resourceId": "%s" },
                    { "resourceId": "%s" }
                  ]
                }
                """.formatted(catalog.menuResourceId(), catalog.pageResourceId());

        HttpResponse<String> replaced = http.exchange(
                "PUT", permissionsPath, Map.of("Content-Type", "application/json"), replaceBody);
        LOGGER.info("PUT {} status={} body={}", permissionsPath, replaced.statusCode(), replaced.body());
        assertThat(replaced.statusCode()).as(replaced.body()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "02-after-replace");

        Long grantCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_permission_grant WHERE subject_id = ?",
                Long.class,
                roleId);
        assertThat(grantCount).isEqualTo(2L);

        List<String> dbResourceKeys = jdbcTemplate.queryForList(
                """
                SELECT r.resource_key FROM nx_permission_grant g
                JOIN nx_console_catalog_resource r ON g.resource_id = r.resource_id
                WHERE g.subject_id = ?
                ORDER BY r.resource_key
                """,
                String.class,
                roleId);
        assertThat(dbResourceKeys).containsExactly(catalog.menuResourceKey(), catalog.pageResourceKey());

        HttpResponse<String> listed = http.get(permissionsPath);
        LOGGER.info("GET {} status={} body={}", permissionsPath, listed.statusCode(), listed.body());
        assertThat(listed.statusCode()).isEqualTo(200);
        assertThat(listed.body()).contains(catalog.menuResourceId());
        assertThat(listed.body()).contains(catalog.pageResourceId());

        String menuOnlyBody = """
                {
                  "grants": [
                    { "resourceId": "%s" }
                  ]
                }
                """.formatted(catalog.menuResourceId());

        HttpResponse<String> menuOnly = http.exchange(
                "PUT", permissionsPath, Map.of("Content-Type", "application/json"), menuOnlyBody);
        LOGGER.info("PUT menu-only status={} body={}", menuOnly.statusCode(), menuOnly.body());
        assertThat(menuOnly.statusCode()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "03-after-menu-only-replace");

        grantCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_permission_grant WHERE subject_id = ?",
                Long.class,
                roleId);
        assertThat(grantCount).isEqualTo(1L);

        HttpResponse<String> cleared = http.exchange(
                "PUT",
                permissionsPath,
                Map.of("Content-Type", "application/json"),
                "{\"grants\":[]}");
        LOGGER.info("PUT clear status={} body={}", cleared.statusCode(), cleared.body());
        assertThat(cleared.statusCode()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "04-after-clear");

        grantCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_permission_grant WHERE subject_id = ?",
                Long.class,
                roleId);
        assertThat(grantCount).isZero();

        HttpResponse<String> afterClear = http.get(permissionsPath);
        assertThat(afterClear.body()).contains("\"grants\":[]");

        LOGGER.info(
                "final nx_permission_grant rows: {}",
                Jsons.toJson(jdbcTemplate.queryForList(
                        "SELECT * FROM nx_permission_grant ORDER BY grant_id")));
    }

    private String createRole(String roleCode, String roleName) throws Exception {
        String createBody = """
                {
                  "roleName": "%s",
                  "roleCode": "%s",
                  "ownerType": "WORKSPACE",
                  "ownerId": "%s",
                  "securityRealm": "TENANT",
                  "description": "permission grant IT",
                  "sortOrder": 5
                }
                """.formatted(roleName, roleCode, WORKSPACE_ID);

        HttpResponse<String> created = http.postJson(ROLES_BASE, createBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);

        return jdbcTemplate.queryForObject(
                "SELECT role_id FROM nx_role WHERE role_code = ?",
                String.class,
                roleCode);
    }
}
