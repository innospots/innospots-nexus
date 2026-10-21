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
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.endpoint.CurrentAuthorizationEndpoint;
import com.innospots.nexus.console.permission.endpoint.GrantManagementEndpoint;
import com.innospots.nexus.console.permission.endpoint.OrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.permission.endpoint.PlatformCurrentAuthorizationEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformGrantManagementEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformOrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.permission.support.PermissionGrantDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.permission.support.SyncedCatalogResourceIds;
import com.innospots.nexus.sample.spring.platform.permission.support.SyncedCatalogResourceIds.UserAndRoleMenus;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PlatformGrantManagementEndpoint}、{@link PlatformOrganizationUnitGrantManagementEndpoint}、
 * {@link PlatformCurrentAuthorizationEndpoint} 全量 HTTP 集成测试（H2 + 同步目录资源）。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_PERMISSION_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformPermissionEndpointIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(PlatformPermissionEndpointIntegrationTest.class);

    private static final String DEV_USER_ID = "1";

    private static final String ORG_UNIT_ID = "plt-org-unit-sample";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConsoleCatalogService catalogService;

    @Autowired
    private GrantManagementEndpoint grantManagementEndpoint;

    @Autowired
    private PlatformGrantManagementEndpoint platformGrantManagementEndpoint;

    @Autowired
    private OrganizationUnitGrantManagementEndpoint organizationUnitGrantManagementEndpoint;

    @Autowired
    private PlatformOrganizationUnitGrantManagementEndpoint platformOrganizationUnitGrantManagementEndpoint;

    @Autowired
    private CurrentAuthorizationEndpoint currentAuthorizationEndpoint;

    @Autowired
    private PlatformCurrentAuthorizationEndpoint platformCurrentAuthorizationEndpoint;

    private RoleEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanRolePermissionGraph(jdbcTemplate);
        http = new RoleEndpointHttpClient(port);
        assertThat(grantManagementEndpoint).isSameAs(platformGrantManagementEndpoint);
        assertThat(organizationUnitGrantManagementEndpoint).isSameAs(platformOrganizationUnitGrantManagementEndpoint);
        assertThat(currentAuthorizationEndpoint).isSameAs(platformCurrentAuthorizationEndpoint);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "00-clean");
    }

    @Test
    void rolePermissionReadDoesNotRejectMissingRoleWithPermissionError() throws Exception {
        String path = PlatformConstant.ROLES_PATH + "/rol-not-visible/permissions";

        HttpResponse<String> response = http.get(path);

        LOGGER.info(
                "GET role permissions for unavailable role status={} body={}",
                response.statusCode(),
                response.body());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"grants\":[]");
    }

    @Test
    void rolePermissionEndpointsListReplaceAndPersistGrants() throws Exception {
        UserAndRoleMenus catalog = SyncedCatalogResourceIds.requireUserAndRoleMenus(jdbcTemplate);
        LOGGER.info("catalog resources: {}", Jsons.toJson(catalog));

        String roleId = createRole("perm-role", "Permission Role");
        String path = PlatformConstant.ROLES_PATH + "/" + roleId + "/permissions";

        HttpResponse<String> empty = http.get(path);
        LOGGER.info("GET role permissions (empty) status={} body={}", empty.statusCode(), empty.body());
        assertThat(empty.statusCode()).isEqualTo(200);
        assertThat(empty.body()).contains("\"grants\":[]");
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "role-01-empty", "ROLE", roleId);

        String replaceBody = """
                {
                  "grants": [
                    { "resourceKey": "%s" },
                    { "resourceKey": "%s" }
                  ]
                }
                """.formatted(catalog.userMenuViewGrantKey(), catalog.roleMenuViewGrantKey());

        HttpResponse<String> replaced = http.exchange(
                "PUT", path, Map.of("Content-Type", "application/json"), replaceBody);
        LOGGER.info("PUT role permissions status={} body={}", replaced.statusCode(), replaced.body());
        assertThat(replaced.statusCode()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "role-02-after-replace", "ROLE", roleId);

        assertGrantRows(
                "ROLE",
                roleId,
                2,
                catalog.userMenuViewGrantKey(),
                catalog.roleMenuViewGrantKey());

        HttpResponse<String> listed = http.get(path);
        LOGGER.info("GET role permissions (filled) status={} body={}", listed.statusCode(), listed.body());
        assertThat(listed.statusCode()).isEqualTo(200);
        assertThat(listed.body()).contains(catalog.userMenuViewGrantKey());
        assertThat(listed.body()).contains(catalog.roleMenuViewGrantKey());

        String menuOnlyBody = """
                {
                  "grants": [
                    { "resourceKey": "%s" }
                  ]
                }
                """.formatted(catalog.userMenuViewGrantKey());
        HttpResponse<String> menuOnly = http.exchange(
                "PUT", path, Map.of("Content-Type", "application/json"), menuOnlyBody);
        LOGGER.info("PUT role permissions (menu-only) status={} body={}", menuOnly.statusCode(), menuOnly.body());
        assertThat(menuOnly.statusCode()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "role-03-menu-only", "ROLE", roleId);
        assertGrantRows("ROLE", roleId, 1, catalog.userMenuViewGrantKey());

        HttpResponse<String> cleared = http.exchange(
                "PUT", path, Map.of("Content-Type", "application/json"), "{\"grants\":[]}");
        LOGGER.info("PUT role permissions (clear) status={} body={}", cleared.statusCode(), cleared.body());
        assertThat(cleared.statusCode()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "role-04-after-clear", "ROLE", roleId);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_permission_grant WHERE subject_type = 'ROLE' AND subject_id = ?",
                Long.class,
                roleId)).isZero();

        HttpResponse<String> afterClear = http.get(path);
        assertThat(afterClear.body()).contains("\"grants\":[]");
        LOGGER.info(
                "final nx_permission_grant rows: {}",
                Jsons.toJson(jdbcTemplate.queryForList("SELECT * FROM nx_permission_grant ORDER BY grant_id")));
    }

    @Test
    void rolePermissionsPersistOperationGrantKey() throws Exception {
        String operationGrantKey = requireOperationGrantKey("module:user", "platform.user.create");
        String roleId = createRole("perm-operation-role", "Permission Operation Role");
        String path = PlatformConstant.ROLES_PATH + "/" + roleId + "/permissions";

        String replaceBody = """
                {
                  "grants": [
                    { "resourceKey": "%s" }
                  ]
                }
                """.formatted(operationGrantKey);
        HttpResponse<String> replaced = http.exchange(
                "PUT", path, Map.of("Content-Type", "application/json"), replaceBody);
        LOGGER.info("PUT operation role permission status={} body={}", replaced.statusCode(), replaced.body());
        assertThat(replaced.statusCode()).isEqualTo(200);

        HttpResponse<String> listed = http.get(path);
        LOGGER.info("GET operation role permission status={} body={}", listed.statusCode(), listed.body());
        assertThat(listed.statusCode()).isEqualTo(200);
        assertThat(listed.body()).contains(operationGrantKey);
        assertThat(jdbcTemplate.queryForObject(
                """
                SELECT grant_key FROM nx_permission_grant
                WHERE subject_type = 'ROLE' AND subject_id = ?
                """,
                String.class,
                roleId)).isEqualTo(operationGrantKey);
    }

    @Test
    void organizationUnitPermissionEndpointsListReplaceAndPersistGrants() throws Exception {
        UserAndRoleMenus catalog = SyncedCatalogResourceIds.requireUserAndRoleMenus(jdbcTemplate);
        String path = PlatformConstant.ORGANIZATION_UNIT_PERMISSIONS_PATH.replace("{unitId}", ORG_UNIT_ID);

        HttpResponse<String> empty = http.get(path);
        LOGGER.info("GET org-unit permissions (empty) status={} body={}", empty.statusCode(), empty.body());
        assertThat(empty.statusCode()).isEqualTo(200);
        assertThat(empty.body()).contains("\"grants\":[]");
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "org-01-empty", "ORG_UNIT", ORG_UNIT_ID);

        String replaceBody = """
                {
                  "grants": [
                    { "resourceKey": "%s" }
                  ]
                }
                """.formatted(catalog.userMenuViewGrantKey());

        HttpResponse<String> replaced = http.exchange(
                "PUT", path, Map.of("Content-Type", "application/json"), replaceBody);
        LOGGER.info("PUT org-unit permissions status={} body={}", replaced.statusCode(), replaced.body());
        assertThat(replaced.statusCode()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "org-02-after-replace", "ORG_UNIT", ORG_UNIT_ID);

        assertGrantRows("ORG_UNIT", ORG_UNIT_ID, 1, catalog.userMenuViewGrantKey());

        HttpResponse<String> listed = http.get(path);
        LOGGER.info("GET org-unit permissions (filled) status={} body={}", listed.statusCode(), listed.body());
        assertThat(listed.body()).contains(catalog.userMenuViewGrantKey());

        HttpResponse<String> cleared = http.exchange(
                "PUT", path, Map.of("Content-Type", "application/json"), "{\"grants\":[]}");
        assertThat(cleared.statusCode()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "org-03-after-clear", "ORG_UNIT", ORG_UNIT_ID);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_permission_grant WHERE subject_type = 'ORG_UNIT' AND subject_id = ?",
                Long.class,
                ORG_UNIT_ID)).isZero();
    }

    @Test
    void currentUserPermissionsEndpointReflectsRoleGrantsInDatabase() throws Exception {
        UserAndRoleMenus catalog = SyncedCatalogResourceIds.requireUserAndRoleMenus(jdbcTemplate);
        String roleId = createRole("me-visible-role", "Me Visible Role");
        String rolePermissionsPath = PlatformConstant.ROLES_PATH + "/" + roleId + "/permissions";

        String replaceBody = """
                {
                  "grants": [
                    { "resourceKey": "%s" },
                    { "resourceKey": "%s" }
                  ]
                }
                """.formatted(catalog.userMenuViewGrantKey(), catalog.roleMenuViewGrantKey());
        HttpResponse<String> replaced = http.exchange(
                "PUT",
                rolePermissionsPath,
                Map.of("Content-Type", "application/json"),
                replaceBody);
        assertThat(replaced.statusCode()).isEqualTo(200);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "me-01-role-grants", "ROLE", roleId);

        bindDevUserToRole(roleId);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "me-02-user-bound");

        List<Map<String, Object>> bindings = jdbcTemplate.queryForList(
                """
                SELECT role_id, subject_type, subject_id, owner_type, owner_id, security_realm, created_at
                FROM nx_role_binding
                WHERE subject_type = 'USER' AND subject_id = ?
                """,
                DEV_USER_ID);
        assertThat(bindings).hasSize(1);
        assertThat(bindings.get(0).get("role_id")).isEqualTo(roleId);
        assertThat(bindings.get(0).get("subject_type")).isEqualTo("USER");
        assertThat(bindings.get(0).get("owner_type")).isEqualTo("PLATFORM");
        assertThat(bindings.get(0).get("owner_id")).isNull();
        assertThat(bindings.get(0).get("security_realm")).isEqualTo("PLATFORM");
        assertThat(bindings.get(0).get("created_at")).isNotNull();
        LOGGER.info("nx_role_binding row for dev user: {}", Jsons.toJson(bindings.get(0)));

        HttpResponse<String> emptyMe = http.get(PlatformConstant.ME_PERMISSIONS_PATH);
        LOGGER.info("GET me permissions status={} body={}", emptyMe.statusCode(), emptyMe.body());
        assertThat(emptyMe.statusCode()).isEqualTo(200);
        assertThat(emptyMe.body()).contains(catalog.userMenuResourceId());
        assertThat(emptyMe.body()).contains(catalog.roleMenuResourceId());
        assertThat(emptyMe.body()).contains(catalog.userMenuResourceKey());
        assertThat(emptyMe.body()).contains(catalog.roleMenuResourceKey());

        LOGGER.info(
                "nx_permission_grant for role {}: {}",
                roleId,
                Jsons.toJson(jdbcTemplate.queryForList(
                        """
                        SELECT g.*, r.resource_key FROM nx_permission_grant g
                        LEFT JOIN nx_console_catalog_resource r
                            ON g.grant_key = CONCAT(r.entry_page_key, ':', r.domain_key, '.', r.module_key)
                        WHERE g.subject_type = 'ROLE' AND g.subject_id = ?
                        ORDER BY r.resource_key
                        """,
                        roleId)));
        LOGGER.info(
                "nx_role_binding for user {}: {}",
                DEV_USER_ID,
                Jsons.toJson(jdbcTemplate.queryForList(
                        "SELECT * FROM nx_role_binding WHERE subject_type = 'USER' AND subject_id = ?",
                        DEV_USER_ID)));
    }

    private void assertGrantRows(
            String subjectType,
            String subjectId,
            long expectedCount,
            String... viewGrantKeys
    ) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_permission_grant WHERE subject_type = ? AND subject_id = ?",
                Long.class,
                subjectType,
                subjectId);
        assertThat(count).isEqualTo(expectedCount);

        for (Map<String, Object> row : jdbcTemplate.queryForList(
                """
                SELECT g.grant_id, g.grant_key, g.owner_type, g.owner_id, g.security_realm,
                       g.constraint_definition, g.created_at, r.resource_key, r.resource_type
                FROM nx_permission_grant g
                LEFT JOIN nx_console_catalog_resource r
                    ON g.grant_key = CONCAT(r.entry_page_key, ':', r.domain_key, '.', r.module_key)
                WHERE g.subject_type = ? AND g.subject_id = ?
                ORDER BY r.resource_key
                """,
                subjectType,
                subjectId)) {
            assertThat(row.get("grant_id")).as("grant id").isNotNull();
            assertThat(row.get("owner_type")).isEqualTo("PLATFORM");
            assertThat(row.get("owner_id")).isNull();
            assertThat(row.get("security_realm")).isEqualTo("PLATFORM");
            assertThat(row.get("constraint_definition")).as("catalog v2 has no constraint grants").isNull();
            assertThat(row.get("created_at")).as("grant audit timestamp").isNotNull();
            assertThat(row.get("resource_type")).isEqualTo("MODULE");
            assertThat(row.get("grant_key")).isIn((Object[]) viewGrantKeys);
        }

        if (viewGrantKeys.length > 0) {
            List<String> keys = jdbcTemplate.queryForList(
                    """
                    SELECT g.grant_key FROM nx_permission_grant g
                    WHERE g.subject_type = ? AND g.subject_id = ?
                    ORDER BY g.grant_key
                    """,
                    String.class,
                    subjectType,
                    subjectId);
            assertThat(keys).containsExactlyInAnyOrder(viewGrantKeys);
        }
    }

    private String createRole(String roleCode, String roleName) throws Exception {
        String createBody = """
                {
                  "roleName": "%s",
                  "roleCode": "%s",
                  "description": "platform permission endpoint IT"
                }
                """.formatted(roleName, roleCode);
        HttpResponse<String> created = http.postJson(PlatformConstant.ROLES_PATH, createBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        return jdbcTemplate.queryForObject(
                "SELECT role_id FROM nx_role WHERE role_code = ?",
                String.class,
                roleCode);
    }

    private String requireOperationGrantKey(String moduleResourceKey, String grantName) {
        CatalogNodeVo module = catalogService.permissionCatalog().stream()
                .filter(node -> moduleResourceKey.equals(node.resourceKey()))
                .findFirst()
                .orElseThrow();
        return module.grants().operationGroups().stream()
                .flatMap(group -> group.items().stream())
                .filter(item -> grantName.equals(item.grantName()))
                .map(CatalogViewItemVo::resourceKey)
                .findFirst()
                .orElseThrow();
    }

    private void bindDevUserToRole(String roleId) throws Exception {
        String bindingsPath = PlatformConstant.ROLES_PATH + "/" + roleId + "/bindings";
        String body = """
                {
                  "subjectType": "USER",
                  "subjectIds": ["%s"]
                }
                """.formatted(DEV_USER_ID);
        HttpResponse<String> bound = http.postJson(bindingsPath, body);
        assertThat(bound.statusCode()).as(bound.body()).isEqualTo(200);
    }
}
