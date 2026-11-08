package com.innospots.nexus.sample.spring.platform.bootstrap;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.bootstrap.loader.InitialUserManifestLoader;
import com.innospots.nexus.console.bootstrap.loader.RoleAuthorizationManifestLoader;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.domain.enums.PermissionGrantKeyType;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.support.PlatformDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;
import com.innospots.nexus.sample.spring.platform.support.PlatformSampleRsaPassword;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 {@code console/bootstrap/{roles,users}.yaml} 在启动后写入数据库的完整数据：
 * 资源目录 → 角色 → 角色授权 → 用户 → 用户角色绑定，以及这些数据的真实鉴权效果。
 *
 * <p>该测试开启请求侧鉴权，用三个演示账号登录后对比页面授权、菜单裁剪与接口写权限，
 * 覆盖「初始化数据正确」与「鉴权链路完整」两条主线。</p>
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_BOOTSTRAP_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT,
        "nexus.console.web.security.enabled=true",
        "nexus.console.web.security.dev-session.enabled=false"
})
class ConsoleBootstrapDataIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-bootstrap";

    private static final String ADMIN_LOGIN = "admin";

    private static final String ADMIN_PASSWORD = "Admin12345";

    private static final String USER_LOGIN = "user01";

    private static final String USER_PASSWORD = "User12345";

    private static final String OPERATOR_LOGIN = "opt01";

    private static final String OPERATOR_PASSWORD = "Opt12345";

    private static final String PAGE_ACCESS_PATH =
            "/api/public/page-access/verify";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleAuthorizationManifestLoader roleManifestLoader;

    @Autowired
    private InitialUserManifestLoader userManifestLoader;

    @Autowired
    private ConsoleCatalogService catalogService;

    @Test
    void bootstrapRolesGrantsAndUsersMatchManifest() {
        List<RoleDefinition> roles = roleManifestLoader.loadRoles();
        assertThat(roles).as("roles.yaml must declare bootstrap roles").isNotEmpty();

        // 资源目录：platform 插件 7 个 MODULE + console 插件管理 1 个 MODULE。
        List<Map<String, Object>> catalogModules = jdbcTemplate.queryForList(
                """
                SELECT domain_key, module_key, resource_type, status
                FROM nx_console_catalog_resource
                WHERE resource_type = 'MODULE'
                """);
        assertThat(catalogModules)
                .as("bootstrap catalog modules")
                .hasSize(8)
                .allSatisfy(module -> {
                    assertThat(module.get("status")).isEqualTo("ENABLED");
                    assertThat(module.get("domain_key")).isIn("system", "operation");
                });
        assertThat(catalogModules.stream().map(module -> module.get("module_key")).toList())
                .containsExactlyInAnyOrder(
                        "settings", "theme", "logger", "dictionary", "plugin",
                        "role", "user", "organization");

        Map<String, String> roleIdByCode = new LinkedHashMap<>();
        for (RoleDefinition definition : roles) {
            Map<String, Object> role = jdbcTemplate.queryForMap(
                    """
                    SELECT role_id, role_code, role_name, owner_type, owner_id, security_realm,
                           status, sort_order, built_in, administrator, read_only
                    FROM nx_role
                    WHERE role_code = ? AND owner_type = 'PLATFORM' AND security_realm = 'PLATFORM'
                    """,
                    definition.roleCode());
            assertThat(role.get("role_name")).isEqualTo(definition.roleName());
            assertThat(role.get("owner_type")).isEqualTo("PLATFORM");
            assertThat(role.get("owner_id")).as("platform role ownerId").isNull();
            assertThat(role.get("security_realm")).isEqualTo("PLATFORM");
            assertThat(role.get("status")).isEqualTo("ENABLED");
            assertThat(role.get("sort_order")).isEqualTo(definition.order());
            assertThat(role.get("built_in")).isEqualTo(true);
            assertThat(role.get("read_only")).isEqualTo(true);
            assertThat(role.get("administrator")).isEqualTo(definition.admin());
            roleIdByCode.put(definition.roleCode(), String.valueOf(role.get("role_id")));
        }
        assertThat(roleIdByCode.values()).doesNotHaveDuplicates();

        Map<String, PermissionGrantKeyType> catalogGrantKeyTypes = catalogService.grantKeyTypes();
        for (RoleDefinition definition : roles) {
            Set<String> keys = new LinkedHashSet<>(
                    RoleAuthorizationManifestLoader.expandGrantKeys(definition.grants()));
            String roleId = roleIdByCode.get(definition.roleCode());
            List<Map<String, Object>> grants = jdbcTemplate.queryForList(
                    """
                    SELECT grant_key, grant_key_type, owner_type, owner_id, security_realm,
                           subject_type, read_only
                    FROM nx_permission_grant
                    WHERE subject_type = 'ROLE' AND subject_id = ?
                    ORDER BY grant_key
                    """,
                    roleId);
            assertThat(grants.stream().map(grantRow -> grantRow.get("grant_key")).toList())
                    .as("grants of role %s", definition.roleCode())
                    .containsExactlyInAnyOrderElementsOf(keys);
            assertThat(grants).allSatisfy(grant -> {
                assertThat(grant.get("subject_type")).isEqualTo("ROLE");
                assertThat(grant.get("owner_type")).isEqualTo("PLATFORM");
                assertThat(grant.get("owner_id")).as("platform grant ownerId").isNull();
                assertThat(grant.get("security_realm")).isEqualTo("PLATFORM");
                assertThat(grant.get("read_only")).isEqualTo(true);
                String grantKey = String.valueOf(grant.get("grant_key"));
                assertThat(catalogGrantKeyTypes)
                        .as("grant key must exist in the permission catalog: %s", grantKey)
                        .containsKey(grantKey);
                assertThat(grant.get("grant_key_type"))
                        .as("grant key type of %s", grantKey)
                        .isEqualTo(catalogGrantKeyTypes.get(grantKey).code());
            });
        }

        InitialUserManifestLoader.LoadedInitialUsers loadedUsers = userManifestLoader.load();
        assertThat(loadedUsers.enabled()).isTrue();
        assertThat(loadedUsers.users()).isNotEmpty();
        for (InitialUser user : loadedUsers.users()) {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    """
                    SELECT admin_user_id, security_realm, display_name, email, mobile, status
                    FROM nx_admin_user
                    WHERE login_name = ?
                    """,
                    user.loginName());
            String adminUserId = String.valueOf(row.get("admin_user_id"));
            assertThat(row.get("security_realm")).isEqualTo("PLATFORM");
            assertThat(row.get("status")).isEqualTo("ACTIVE");
            assertThat(row.get("display_name")).isEqualTo(user.displayName());
            assertThat(row.get("email")).isEqualTo(user.email());
            assertThat(row.get("mobile")).isEqualTo(user.mobile());

            Long credentials = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM nx_user_credential
                    WHERE subject_id = ? AND security_realm = 'PLATFORM' AND credential_kind = 'PASSWORD'
                    """,
                    Long.class,
                    adminUserId);
            assertThat(credentials).as("password credential of %s", user.loginName()).isEqualTo(1L);

            Set<String> expectedRoleIds = new LinkedHashSet<>();
            for (String roleCode : user.roleCodes()) {
                expectedRoleIds.add(roleIdByCode.get(roleCode));
            }
            List<Map<String, Object>> bindings = jdbcTemplate.queryForList(
                    """
                    SELECT role_id, subject_type, subject_id, owner_type, owner_id, security_realm
                    FROM nx_role_binding
                    WHERE subject_type = 'USER' AND subject_id = ?
                    """,
                    adminUserId);
            assertThat(bindings.stream().map(bindingRow -> String.valueOf(bindingRow.get("role_id"))).toList())
                    .as("role bindings of %s", user.loginName())
                    .containsExactlyInAnyOrderElementsOf(expectedRoleIds);
            assertThat(bindings).allSatisfy(binding -> {
                assertThat(binding.get("subject_id")).isEqualTo(adminUserId);
                assertThat(binding.get("owner_type")).isEqualTo("PLATFORM");
                assertThat(binding.get("owner_id")).as("platform binding ownerId").isNull();
                assertThat(binding.get("security_realm")).isEqualTo("PLATFORM");
            });
        }

        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-bootstrap-data",
                Map.of(
                        "nx_role", "role_code",
                        "nx_role_binding", "binding_id",
                        "nx_permission_grant", "grant_key",
                        "nx_admin_user", "login_name"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_admin_user", Long.class)).isEqualTo(3L);
        assertThat(jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM nx_role_binding WHERE subject_type = 'USER'
                """, Long.class)).isEqualTo(3L);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_permission_grant", Long.class)).isEqualTo(59L);

        assertNoDuplicatedGrants();
    }

    @Test
    void bootstrapAccountsDrivePageAccessNavigationAndEndpointAuthorization() throws Exception {
        PlatformEndpointHttpClient http = new PlatformEndpointHttpClient(port);

        String adminToken = login(http, ADMIN_LOGIN, ADMIN_PASSWORD);
        String userToken = login(http, USER_LOGIN, USER_PASSWORD);
        String operatorToken = login(http, OPERATOR_LOGIN, OPERATOR_PASSWORD);

        // 三个账号都有 operation.role:view → 角色列表可读。
        assertThat(status(http, PlatformConstant.ROLES_PATH, adminToken)).isEqualTo(200);
        assertThat(status(http, PlatformConstant.ROLES_PATH, userToken)).isEqualTo(200);
        assertThat(status(http, PlatformConstant.ROLES_PATH, operatorToken)).isEqualTo(200);

        // 只有 sys_admin / operator 有 operation.role:create；user 仅有 view。
        assertThat(status(http, PlatformConstant.ROLES_PATH, userToken, "POST", roleCreateBody("bootstrap-denied")))
                .as("standard user must not create roles")
                .isEqualTo(403);
        assertThat(status(http, PlatformConstant.ROLES_PATH, operatorToken, "POST", roleCreateBody("bootstrap-operator")))
                .as("operator role grants create")
                .isEqualTo(200);
        assertThat(status(http, PlatformConstant.ROLES_PATH, adminToken, "POST", roleCreateBody("bootstrap-admin")))
                .as("administrator bypasses resource grants")
                .isEqualTo(200);

        // UI 依赖 readOnly 字段决定是否展示状态/删除/授权保存等变更操作。
        HttpResponse<String> rolePage = http.exchange(
                "GET",
                PlatformConstant.ROLES_PATH + "?pageNo=1&pageSize=50",
                Map.of("Authorization", "Bearer " + adminToken),
                null);
        assertThat(rolePage.statusCode()).as(rolePage.body()).isEqualTo(200);
        JsonNode roleRecords = Jsons.mapper().readTree(rolePage.body()).path("data").path("records");
        JsonNode sysAdmin = null;
        for (JsonNode record : roleRecords) {
            if ("sys_admin".equals(record.path("roleCode").asText())) {
                sysAdmin = record;
                break;
            }
        }
        assertThat(sysAdmin).as("sys_admin must be returned by the role API").isNotNull();
        assertThat(sysAdmin.path("readOnly").asBoolean())
                .as("bootstrap roles must expose readOnly=true")
                .isTrue();

        // 角色授权接口挂在 operation.role 模块：view 可读、update 可写。
        String operatorRoleId = jdbcTemplate.queryForObject(
                "SELECT role_id FROM nx_role WHERE role_code = 'bootstrap-operator'",
                String.class);
        String permissionsPath = PlatformConstant.ROLES_PATH + "/" + operatorRoleId + "/permissions";
        assertThat(status(http, permissionsPath, userToken))
                .as("role view permission may read role grants")
                .isEqualTo(200);
        assertThat(status(http, permissionsPath, userToken, "PUT", "{\"grants\":[]}"))
                .as("role view only must not replace role grants")
                .isEqualTo(403);
        assertThat(status(http, permissionsPath, operatorToken, "PUT", "{\"grants\":[]}"))
                .as("operator holds operation.role:update")
                .isEqualTo(200);

        // 页面授权：operation.role 页面允许普通用户；system.settings 页面只属于系统管理员。
        assertThat(pageAllowed(http, userToken, "operation", "role", "platform-role-main", "view"))
                .as("standard user holds operation.role:view")
                .isTrue();
        assertThat(pageAllowed(http, userToken, "system", "settings", "system-settings", "view"))
                .as("standard user must not open system settings")
                .isFalse();
        assertThat(pageAllowed(http, adminToken, "system", "settings", "system-settings", "view"))
                .as("administrator may open system settings")
                .isTrue();

        // 导航裁剪：普通用户看不到 system 分组；管理员可以看到。
        Set<String> userRoots = navigationRootKeys(http, userToken);
        assertThat(userRoots).as("user navigation").contains("home", "operations");
        assertThat(userRoots).as("user navigation must hide system").doesNotContain("system");
        Set<String> adminRoots = navigationRootKeys(http, adminToken);
        assertThat(adminRoots).as("administrator navigation").contains("home", "operations", "system");
    }

    private void assertNoDuplicatedGrants() {
        List<Map<String, Object>> duplicates = jdbcTemplate.queryForList(
                """
                SELECT subject_type, subject_id, grant_key, COUNT(*) AS total
                FROM nx_permission_grant
                GROUP BY subject_type, subject_id, grant_key
                HAVING COUNT(*) > 1
                """);
        assertThat(duplicates).as("duplicated permission grants: %s", Jsons.toJson(duplicates)).isEmpty();
    }

    private String login(
            PlatformEndpointHttpClient http,
            String loginName,
            String rawPassword
    ) throws Exception {
        String body = """
                {
                  "login": "%s",
                  "encryptedPassword": "%s"
                }
                """.formatted(loginName, PlatformSampleRsaPassword.encrypt(rawPassword));
        HttpResponse<String> response = http.postJson(PlatformConstant.PUBLIC_AUTH_PATH + "/login", body);
        assertThat(response.statusCode()).as("login %s: %s", loginName, response.body()).isEqualTo(200);
        JsonNode data = Jsons.mapper().readTree(response.body()).path("data");
        String token = data.path("accessToken").asText();
        assertThat(token).as("access token of %s", loginName).isNotBlank();
        return token;
    }

    private int status(PlatformEndpointHttpClient http, String path, String token) throws Exception {
        return status(http, path, token, "GET", null);
    }

    private int status(
            PlatformEndpointHttpClient http,
            String path,
            String token,
            String method,
            String body
    ) throws Exception {
        Map<String, String> headers = method.equals("GET")
                ? Map.of("Authorization", "Bearer " + token)
                : Map.of(
                        "Authorization", "Bearer " + token,
                        "Content-Type", "application/json");
        return http.exchange(method, path, headers, body).statusCode();
    }

    private boolean pageAllowed(
            PlatformEndpointHttpClient http,
            String token,
            String domainKey,
            String moduleKey,
            String pageKey,
            String grantGroup
    ) throws Exception {
        String body = """
                {
                  "domainKey": "%s",
                  "moduleKey": "%s",
                  "pageKey": "%s",
                  "grantGroup": "%s",
                  "accessMode": "grant"
                }
                """.formatted(domainKey, moduleKey, pageKey, grantGroup);
        HttpResponse<String> response = http.exchange(
                "POST",
                PAGE_ACCESS_PATH,
                Map.of(
                        "Authorization", "Bearer " + token,
                        "Content-Type", "application/json"),
                body);
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        return Jsons.mapper().readTree(response.body()).path("data").path("allowed").asBoolean();
    }

    private Set<String> navigationRootKeys(
            PlatformEndpointHttpClient http,
            String token
    ) throws Exception {
        HttpResponse<String> response = http.exchange(
                "GET",
                PlatformConstant.NAVIGATION_MENUS_PATH,
                Map.of("Authorization", "Bearer " + token),
                null);
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        JsonNode data = Jsons.mapper().readTree(response.body()).path("data");
        assertThat(data.isArray()).isTrue();
        Set<String> keys = new LinkedHashSet<>();
        for (JsonNode menu : data) {
            keys.add(menu.path("menuKey").asText());
        }
        return keys;
    }

    private static String roleCreateBody(String roleCode) {
        return """
                {
                  "roleName": "%s",
                  "roleCode": "%s",
                  "description": "console bootstrap integration test"
                }
                """.formatted(roleCode, roleCode);
    }
}
