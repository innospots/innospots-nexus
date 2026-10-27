package com.innospots.nexus.sample.spring.platform.navigation;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.permission.support.PermissionGrantDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.permission.support.SyncedCatalogResourceIds;
import com.innospots.nexus.sample.spring.platform.permission.support.SyncedCatalogResourceIds.UserAndRoleMenus;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;
import com.innospots.nexus.sample.spring.platform.support.PlatformSampleRsaPassword;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link com.innospots.nexus.platform.navigation.endpoint.PlatformNavigationMenuEndpoint}
 * HTTP 集成测试：真实平台登录后，导航菜单由 {@code nx_menu} 菜单记录、
 * 角色授权（{@code nx_permission_grant}）与角色绑定（{@code nx_role_binding}）共同驱动。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_NAVIGATION_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT,
        "nexus.console.web.security.enabled=true",
        "nexus.console.web.security.dev-session.enabled=false"
})
class PlatformNavigationMenuEndpointIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(PlatformNavigationMenuEndpointIntegrationTest.class);

    private static final String OUTPUT_DIR = "sample-platform-navigation";

    private static final String DEV_USER_ID = PlatformIntegrationTestSupport.DEV_PLATFORM_USER_ID;

    private static final String DEV_LOGIN_NAME = "dev";

    private static final String DEV_PASSWORD = "Navigation-Pass-1!";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private ConsoleCatalogService catalogService;

    private RoleEndpointHttpClient http;

    private String accessToken;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanRolePermissionGraph(jdbcTemplate);
        PlatformIntegrationTestSupport.ensureDevSessionPlatformUser(
                jdbcTemplate,
                credentialService,
                DEV_PASSWORD);
        PlatformIntegrationTestSupport.ensurePlatformAdministratorRoleAndBindUser(
                jdbcTemplate,
                DEV_USER_ID);
        seedNavigationMenu();
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
    void loginThenNavigationMenusReturnAuthorizedTree() throws Exception {
        accessToken = loginAsDevUser();

        UserAndRoleMenus catalog = SyncedCatalogResourceIds.requireUserAndRoleMenus(jdbcTemplate);
        String roleId = createRole("nav-visible-role", "Nav Visible Role");
        grantResourcesToRole(roleId, catalog.userMenuViewGrantKey());
        PlatformIntegrationTestSupport.replacePlatformUserRoleBindings(
                jdbcTemplate,
                DEV_USER_ID,
                roleId);
        accessToken = loginAsDevUser();
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "01-grants-bound", "ROLE", roleId);

        assertNavigationGrantRows(
                roleId,
                catalog.userMenuViewGrantKey(),
                catalog.userMenuResourceKey());

        HttpResponse<String> menus = http.exchange(
                "GET",
                PlatformConstant.NAVIGATION_MENUS_PATH,
                bearerHeaders(),
                null);
        LOGGER.info(
                "GET navigation menus after login login={} status={} body={}",
                DEV_LOGIN_NAME,
                menus.statusCode(),
                menus.body());
        assertThat(menus.statusCode()).as(menus.body()).isEqualTo(200);
        assertThat(menus.body()).contains("\"success\":true");

        JsonNode data = Jsons.mapper().readTree(menus.body()).get("data");
        assertThat(data).as("navigation data array").isNotNull();
        assertThat(data.isArray()).as("navigation data must be an array").isTrue();
        assertNavigationTree(data);

        JsonNode userMenu = findMenuByKey(data, "platform-user-main");
        assertThat(userMenu).as("granted catalog menu must be visible").isNotNull();
        assertThat(userMenu.get("menuName").asText()).contains("用户列表");
        assertThat(userMenu.get("routePath").asText())
                .isEqualTo("/page/platform/user/platform-user-main");
        assertThat(userMenu.get("componentKey").asText()).isEqualTo("platform/user/index");
        assertThat(userMenu.get("openMode").asText()).isEqualTo("INTERNAL");
        assertThat(userMenu.get("children").isArray()).as("children must be an array").isTrue();

        assertThat(findMenuByKey(data, "platform-dictionary-main"))
                .as("catalog page resource without a grant must stay hidden")
                .isNull();

        logAndPersistNavigationTree(data);

        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-after-navigation-get",
                Map.of(
                        "nx_permission_grant",
                        "grant_id",
                        "nx_role_binding",
                        "binding_id",
                        "nx_role",
                        "role_code"));

        Map<String, Object> dbSummary = Map.of(
                "roleId", roleId,
                "grantRows", jdbcTemplate.queryForList(
                        """
                        SELECT g.subject_type, g.subject_id, r.resource_key
                        FROM nx_permission_grant g
                        LEFT JOIN nx_console_catalog_resource r
                            ON g.grant_key = CONCAT(r.entry_page_key, ':', r.domain_key, '.', r.module_key)
                        WHERE g.subject_type = 'ROLE' AND g.subject_id = ?
                        """,
                        roleId),
                "bindingRows", jdbcTemplate.queryForList(
                        """
                        SELECT role_id, subject_type, subject_id, owner_type, security_realm
                        FROM nx_role_binding
                        WHERE subject_type = 'USER' AND subject_id = ?
                        """,
                        DEV_USER_ID),
                "navigationMenuKeys", menuKeys(data));
        LOGGER.info("navigation integration DB + response summary: {}", Jsons.toJson(dbSummary));
    }

    @Test
    void operationGrantDoesNotRevealNavigationMenu() throws Exception {
        accessToken = loginAsDevUser();

        String operationGrantKey = requireOperationGrantKey("module:user", "platform.user.create");
        String roleId = createRole("nav-operation-role", "Nav Operation Role");
        grantResourcesToRole(roleId, operationGrantKey);
        PlatformIntegrationTestSupport.replacePlatformUserRoleBindings(
                jdbcTemplate,
                DEV_USER_ID,
                roleId);
        accessToken = loginAsDevUser();
        PermissionGrantDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                "operation-only-grant",
                "ROLE",
                roleId);

        HttpResponse<String> menus = http.exchange(
                "GET",
                PlatformConstant.NAVIGATION_MENUS_PATH,
                bearerHeaders(),
                null);
        LOGGER.info(
                "GET navigation menus with operation-only grant status={} body={}",
                menus.statusCode(),
                menus.body());
        assertThat(menus.statusCode()).as(menus.body()).isEqualTo(200);

        JsonNode data = Jsons.mapper().readTree(menus.body()).get("data");
        assertThat(findMenuByKey(data, "platform-user-main"))
                .as("operation grants must not reveal the user page")
                .isNull();
    }

    private String loginAsDevUser() throws Exception {
        String loginBody = """
                {
                  "login": "%s",
                  "encryptedPassword": "%s"
                }
                """.formatted(DEV_LOGIN_NAME, PlatformSampleRsaPassword.encrypt(DEV_PASSWORD));
        HttpResponse<String> login = http.exchange(
                "POST",
                PlatformConstant.PUBLIC_AUTH_PATH + "/login",
                Map.of("Content-Type", "application/json"),
                loginBody);
        assertThat(login.statusCode()).as("public login status").isEqualTo(200);

        JsonNode data = Jsons.mapper().readTree(login.body()).path("data");
        String token = data.path("accessToken").asText();
        assertThat(token).as("access token").isNotBlank();
        LOGGER.info(
                "platform login succeeded login={} realm={} tokenType={} accessTokenPresent=true",
                DEV_LOGIN_NAME,
                data.path("realm").asText(),
                data.path("tokenType").asText());
        return token;
    }

    private Map<String, String> jsonHeaders() {
        return Map.of(
                "Content-Type", "application/json",
                "Authorization", bearerValue());
    }

    private Map<String, String> bearerHeaders() {
        return Map.of("Authorization", bearerValue());
    }

    private String bearerValue() {
        assertThat(accessToken).as("login must happen before authenticated requests").isNotBlank();
        return "Bearer " + accessToken;
    }

    private void assertNavigationTree(JsonNode menus) {
        assertThat(rootMenuKeys(menus))
                .as("navigation root menu keys")
                .containsExactly("platform-user-main", "home", "operations", "system");
        assertMenuTreeShape(menus, new LinkedHashSet<>());

        JsonNode operations = findMenuByKey(menus, "operations");
        assertThat(operations).as("operations group").isNotNull();
        assertThat(menuKeys(operations.path("children")))
                .as("operations children")
                .containsExactly(
                        "nav-organization-tenants",
                        "nav-platform-users",
                        "nav-platform-invites",
                        "nav-platform-access-requests");

        JsonNode system = findMenuByKey(menus, "system");
        assertThat(system).as("system group").isNotNull();
        assertThat(menuKeys(system.path("children")))
                .as("system children")
                .containsExactly(
                        "nav-menus",
                        "nav-dictionaries",
                        "nav-roles",
                        "nav-audit",
                        "nav-plugins");
    }

    private void assertMenuTreeShape(JsonNode menus, Set<String> visitedKeys) {
        for (JsonNode menu : menus) {
            assertThat(menu.isObject()).as("navigation node must be an object").isTrue();
            String menuKey = menu.path("menuKey").asText();
            assertThat(menuKey).as("menuKey").isNotBlank();
            assertThat(visitedKeys.add(menuKey))
                    .as("menuKey must be unique in the navigation tree: %s", menuKey)
                    .isTrue();
            assertThat(menu.path("menuName").asText()).as("menuName of %s", menuKey).isNotBlank();
            assertThat(menu.path("openMode").asText())
                    .as("openMode of %s", menuKey)
                    .isEqualTo("INTERNAL");
            JsonNode children = menu.path("children");
            assertThat(children.isArray())
                    .as("children of %s must be an array", menuKey)
                    .isTrue();
            assertMenuTreeShape(children, visitedKeys);
        }
    }

    private List<String> rootMenuKeys(JsonNode menus) {
        List<String> keys = new ArrayList<>();
        for (JsonNode menu : menus) {
            keys.add(menu.path("menuKey").asText());
        }
        return keys;
    }

    private void logAndPersistNavigationTree(JsonNode data) throws IOException {
        LOGGER.info("navigation menu tree JSON: {}", Jsons.toJson(data));
        Path outputFile = Path.of(
                "target",
                "test-output",
                OUTPUT_DIR,
                "03-navigation-tree.json");
        Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, data.toPrettyString());
    }

    private void assertNavigationGrantRows(
            String roleId,
            String viewGrantKey,
            String resourceKey
    ) {
        List<Map<String, Object>> grants = jdbcTemplate.queryForList(
                """
                SELECT owner_type, owner_id, security_realm, grant_key
                FROM nx_permission_grant
                WHERE subject_type = 'ROLE' AND subject_id = ?
                """,
                roleId);
        assertThat(grants).hasSize(1);
        assertThat(grants.get(0).get("grant_key")).isEqualTo(viewGrantKey);
        assertThat(grants.get(0).get("owner_type")).isEqualTo("PLATFORM");
        assertThat(grants.get(0).get("owner_id")).isNull();
        assertThat(grants.get(0).get("security_realm")).isEqualTo("PLATFORM");

        Map<String, Object> resource = jdbcTemplate.queryForMap(
                """
                SELECT resource_key, resource_type
                FROM nx_console_catalog_resource
                WHERE resource_key = ?
                """,
                resourceKey);
        assertThat(resource.get("resource_key")).isEqualTo(resourceKey);
        assertThat(resource.get("resource_type")).isEqualTo("MODULE");

        List<Map<String, Object>> bindings = jdbcTemplate.queryForList(
                """
                SELECT role_id, subject_type, subject_id, owner_type, owner_id, security_realm
                FROM nx_role_binding
                WHERE role_id = ? AND subject_type = 'USER' AND subject_id = ?
                """,
                roleId,
                DEV_USER_ID);
        assertThat(bindings).hasSize(1);
        assertThat(bindings.get(0).get("owner_type")).isEqualTo("PLATFORM");
        assertThat(bindings.get(0).get("owner_id")).isNull();
        assertThat(bindings.get(0).get("security_realm")).isEqualTo("PLATFORM");

        Long grantCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_permission_grant WHERE subject_type = 'ROLE' AND subject_id = ?",
                Long.class,
                roleId);
        assertThat(grantCount).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT grant_key FROM nx_permission_grant WHERE subject_type = 'ROLE' AND subject_id = ?",
                String.class,
                roleId)).isEqualTo(viewGrantKey);

        Map<String, Object> roleRow = jdbcTemplate.queryForMap(
                """
                SELECT owner_type, owner_id, security_realm, status
                FROM nx_role WHERE role_id = ?
                """,
                roleId);
        assertThat(roleRow.get("owner_type")).isEqualTo("PLATFORM");
        assertThat(roleRow.get("owner_id")).isNull();
        assertThat(roleRow.get("security_realm")).isEqualTo("PLATFORM");
        assertThat(roleRow.get("status")).isEqualTo("ENABLED");
    }

    private JsonNode findMenuByKey(JsonNode menus, String menuKey) {
        for (JsonNode menu : menus) {
            if (menuKey.equals(menu.path("menuKey").asText())) {
                return menu;
            }
            JsonNode found = findMenuByKey(menu.path("children"), menuKey);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private List<String> menuKeys(JsonNode menus) {
        List<String> keys = new ArrayList<>();
        for (JsonNode menu : menus) {
            keys.add(menu.path("menuKey").asText());
            keys.addAll(menuKeys(menu.path("children")));
        }
        return keys;
    }

    private String createRole(String roleCode, String roleName) throws Exception {
        String createBody = """
                {
                  "roleName": "%s",
                  "roleCode": "%s",
                  "description": "platform navigation endpoint IT"
                }
                """.formatted(roleName, roleCode);
        HttpResponse<String> created = http.exchange(
                "POST",
                PlatformConstant.ROLES_PATH,
                jsonHeaders(),
                createBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        return jdbcTemplate.queryForObject(
                "SELECT role_id FROM nx_role WHERE role_code = ?",
                String.class,
                roleCode);
    }

    private void grantResourcesToRole(String roleId, String... grantKeys) throws Exception {
        String path = PlatformConstant.ROLES_PATH + "/" + roleId + "/permissions";
        String grants = java.util.Arrays.stream(grantKeys)
                .map(key -> "{ \"resourceKey\": \"" + key + "\" }")
                .collect(java.util.stream.Collectors.joining(","));
        String body = "{\"grants\":[" + grants + "]}";
        HttpResponse<String> replaced = http.exchange(
                "PUT",
                path,
                jsonHeaders(),
                body);
        assertThat(replaced.statusCode()).as(replaced.body()).isEqualTo(200);
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

    private void seedNavigationMenu() {
        jdbcTemplate.update(
                """
                DELETE FROM nx_menu
                WHERE owner_type = 'PLATFORM' AND security_realm = 'PLATFORM'
                """);
        jdbcTemplate.update(
                """
                INSERT INTO nx_menu (
                    menu_id, parent_id, menu_key, menu_name, menu_type, route_path,
                    component_key, redirect_path, external_url, icon, open_mode,
                    visible, status, sort_order, built_in, owner_type, owner_id,
                    security_realm, created_at, updated_at
                ) VALUES (
                    'MNUNAVUSERMAIN', NULL, 'platform-user-main', '用户列表', 'PAGE',
                    '/page/platform/user/platform-user-main', 'platform/user/index',
                    NULL, NULL, 'users', 'INTERNAL', TRUE, 'ENABLED', 0, FALSE,
                    'PLATFORM', NULL, 'PLATFORM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """);
    }
}
