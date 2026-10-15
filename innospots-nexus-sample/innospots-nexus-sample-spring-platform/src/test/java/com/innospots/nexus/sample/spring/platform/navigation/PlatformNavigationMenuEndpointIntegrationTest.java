package com.innospots.nexus.sample.spring.platform.navigation;

import java.net.http.HttpResponse;
import java.util.Map;

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

import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.permission.support.PermissionGrantDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.permission.support.SyncedCatalogResourceIds;
import com.innospots.nexus.sample.spring.platform.permission.support.SyncedCatalogResourceIds.UserMainMenuPageIds;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link com.innospots.nexus.platform.navigation.endpoint.PlatformNavigationMenuEndpoint} HTTP 集成测试。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_NAVIGATION_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformNavigationMenuEndpointIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(PlatformNavigationMenuEndpointIntegrationTest.class);

    private static final String OUTPUT_DIR = "sample-platform-navigation";

    private static final String DEV_USER_ID = PlatformIntegrationTestSupport.DEV_PLATFORM_USER_ID;

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RoleEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanRolePermissionGraph(jdbcTemplate);
        http = new RoleEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of(
                        "nx_permission_grant",
                        "grant_id",
                        "nx_role_binding",
                        "binding_id",
                        "nx_role",
                        "role_code"));
    }

    @Test
    void navigationMenusReflectRoleGrantsForDevSessionUser() throws Exception {
        UserMainMenuPageIds catalog = SyncedCatalogResourceIds.requireUserMainMenuAndPage(jdbcTemplate);
        String roleId = createRole("nav-visible-role", "Nav Visible Role");
        grantMenuAndPageToRole(roleId, catalog.menuResourceId(), catalog.pageResourceId());
        bindDevUserToRole(roleId);
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "01-grants-bound", "ROLE", roleId);

        HttpResponse<String> menus = http.get(PlatformConstant.NAVIGATION_MENUS_PATH);
        LOGGER.info("GET navigation menus status={} body={}", menus.statusCode(), menus.body());
        assertThat(menus.statusCode()).as(menus.body()).isEqualTo(200);
        assertThat(menus.body()).contains("user-main");
        assertThat(menus.body()).contains("用户列表");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-navigation-get",
                Map.of("nx_permission_grant", "grant_id", "nx_role_binding", "binding_id"));
    }

    private String createRole(String roleCode, String roleName) throws Exception {
        String createBody = """
                {
                  "roleName": "%s",
                  "roleCode": "%s",
                  "description": "platform navigation endpoint IT"
                }
                """.formatted(roleName, roleCode);
        HttpResponse<String> created = http.postJson(PlatformConstant.ROLES_PATH, createBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        return jdbcTemplate.queryForObject(
                "SELECT role_id FROM nx_role WHERE role_code = ?",
                String.class,
                roleCode);
    }

    private void grantMenuAndPageToRole(String roleId, String menuResourceId, String pageResourceId)
            throws Exception {
        String path = PlatformConstant.ROLES_PATH + "/" + roleId + "/permissions";
        String body = """
                {
                  "grants": [
                    { "resourceId": "%s" },
                    { "resourceId": "%s" }
                  ]
                }
                """.formatted(menuResourceId, pageResourceId);
        HttpResponse<String> replaced = http.exchange(
                "PUT",
                path,
                Map.of("Content-Type", "application/json"),
                body);
        assertThat(replaced.statusCode()).as(replaced.body()).isEqualTo(200);
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
