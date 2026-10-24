package com.innospots.nexus.sample.spring.platform.permission;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogPermissionGroupVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;
import com.innospots.nexus.sample.spring.platform.support.PlatformSampleRsaPassword;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 端到端验证：权限目录树 view group {@code resourceKey}、角色授权（
 * {@link com.innospots.nexus.platform.permission.endpoint.PlatformGrantManagementEndpoint}）、
 * 页面访问校验（{@link com.innospots.nexus.console.permission.endpoint.PageAccessEndpoint}）
 * 与 API 资源权限拦截（H2，无 Mock）。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_PERMISSION_GRANT_FLOW_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT,
        "nexus.console.web.security.enabled=true",
        "nexus.console.web.security.dev-session.enabled=false"
})
class PlatformPermissionGrantFlowIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(PlatformPermissionGrantFlowIntegrationTest.class);

    private static final String DEV_USER_ID = PlatformIntegrationTestSupport.DEV_PLATFORM_USER_ID;

    private static final String DEV_LOGIN_NAME = "dev";

    private static final String DEV_PASSWORD = "Grant-Flow-Pass-1!";

    private static final String DICTIONARY_MODULE_RESOURCE_KEY = "module:dictionary";

    private static final String DICTIONARY_PAGE_KEY = "platform-dictionary-main";

    private static final String PERMISSION_CATALOG_PATH = ConsoleConstant.apiPath("/catalog/permission-catalog");

    private static final String PAGE_ACCESS_VERIFY_PATH =
            ConsoleConstant.PUBLIC_API_PREFIX + "/page-access/verify";

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
        PlatformIntegrationTestSupport.ensurePlatformAdministratorRoleAndBindUser(jdbcTemplate, DEV_USER_ID);
        http = new RoleEndpointHttpClient(port);
    }

    @Test
    void permissionCatalogGrantsDrivePageAccessAndEndpointAuthorization() throws Exception {
        accessToken = loginAsDevUser();

        DictionaryGrantKeys dictionary = requireDictionaryGrantKeysFromCatalogHttp();
        assertDictionaryViewGroupInCatalogService(dictionary);

        String restrictedRoleId = createRole("grant-flow-restricted", "Grant Flow Restricted");
        String permissionsPath = PlatformConstant.ROLES_PATH + "/" + restrictedRoleId + "/permissions";

        HttpResponse<String> emptyPermissions = http.exchange("GET", permissionsPath, jsonHeaders(), null);
        assertThat(emptyPermissions.statusCode()).as(emptyPermissions.body()).isEqualTo(200);
        assertThat(emptyPermissions.body()).contains("\"grants\":[]");

        String viewOnlyBody = """
                {
                  "grants": [
                    { "resourceKey": "%s" }
                  ]
                }
                """.formatted(dictionary.viewGroupResourceKey());
        HttpResponse<String> viewGranted = http.exchange(
                "PUT",
                permissionsPath,
                jsonHeaders(),
                viewOnlyBody);
        assertThat(viewGranted.statusCode()).as(viewGranted.body()).isEqualTo(200);

        HttpResponse<String> listed = http.exchange("GET", permissionsPath, jsonHeaders(), null);
        assertThat(listed.statusCode()).as(listed.body()).isEqualTo(200);
        assertThat(listed.body()).contains(dictionary.viewGroupResourceKey());

        PlatformIntegrationTestSupport.replacePlatformUserRoleBindings(
                jdbcTemplate,
                DEV_USER_ID,
                restrictedRoleId);
        accessToken = loginAsDevUser();

        assertPageAccess(dictionary, true);
        assertDictionaryCreateSucceeded();

        HttpResponse<String> mePermissions = http.exchange(
                "GET",
                PlatformConstant.ME_PERMISSIONS_PATH,
                bearerHeaders(),
                null);
        assertThat(mePermissions.statusCode()).as(mePermissions.body()).isEqualTo(200);
        assertThat(mePermissions.body()).contains(DICTIONARY_MODULE_RESOURCE_KEY);
        assertThat(mePermissions.body()).contains(DICTIONARY_PAGE_KEY);
        LOGGER.info("me permissions body={}", mePermissions.body());

        PlatformIntegrationTestSupport.ensurePlatformAdministratorRoleAndBindUser(jdbcTemplate, DEV_USER_ID);
        accessToken = loginAsDevUser();
        String operationOnlyBody = """
                {
                  "grants": [
                    { "resourceKey": "%s" }
                  ]
                }
                """.formatted(dictionary.createOperationResourceKey());
        HttpResponse<String> operationOnly = http.exchange(
                "PUT",
                permissionsPath,
                jsonHeaders(),
                operationOnlyBody);
        assertThat(operationOnly.statusCode()).as(operationOnly.body()).isEqualTo(200);
        PlatformIntegrationTestSupport.replacePlatformUserRoleBindings(
                jdbcTemplate,
                DEV_USER_ID,
                restrictedRoleId);
        accessToken = loginAsDevUser();

        assertPageAccess(dictionary, false);
        assertDictionaryCreateSucceeded();
    }

    @Test
    void deniesPageAccessAndProtectedApiWithoutGrants() throws Exception {
        accessToken = loginAsDevUser();
        DictionaryGrantKeys dictionary = requireDictionaryGrantKeysFromCatalogHttp();

        String restrictedRoleId = createRole("grant-flow-empty", "Grant Flow Empty");
        PlatformIntegrationTestSupport.replacePlatformUserRoleBindings(
                jdbcTemplate,
                DEV_USER_ID,
                restrictedRoleId);
        accessToken = loginAsDevUser();

        assertPageAccess(dictionary, false);
        assertDictionaryCreateForbidden();
    }

    private DictionaryGrantKeys requireDictionaryGrantKeysFromCatalogHttp() throws Exception {
        HttpResponse<String> catalog = http.exchange(
                "GET",
                PERMISSION_CATALOG_PATH,
                bearerHeaders(),
                null);
        assertThat(catalog.statusCode()).as(catalog.body()).isEqualTo(200);
        JsonNode roots = Jsons.mapper().readTree(catalog.body()).path("data");
        assertThat(roots.isArray()).isTrue();

        JsonNode dictionaryModule = null;
        for (JsonNode node : roots) {
            if ("dictionary".equals(node.path("moduleKey").asText())) {
                dictionaryModule = node;
                break;
            }
        }
        assertThat(dictionaryModule).as("dictionary module in permission-catalog").isNotNull();
        assertThat(dictionaryModule.path("resourceKey").asText()).isEqualTo(DICTIONARY_MODULE_RESOURCE_KEY);
        assertThat(dictionaryModule.path("domainKey").asText())
                .isEqualTo(BuiltinPlatformEntryPlugins.DOMAIN_KEY);

        JsonNode viewGroups = dictionaryModule.path("grants").path("viewGroups");
        assertThat(viewGroups.isArray()).as("dictionary viewGroups").isTrue();
        assertThat(viewGroups).hasSize(1);
        JsonNode defaultGroup = viewGroups.get(0);
        assertThat(defaultGroup.path("group").asText()).isEqualTo("default");
        String viewGroupResourceKey = defaultGroup.path("resourceKey").asText();
        assertThat(viewGroupResourceKey).isEqualTo("platform.dictionary:default");
        assertThat(defaultGroup.path("items").isArray()).isTrue();
        assertThat(defaultGroup.path("items")).isEmpty();

        String createKey = requireDictionaryGrantKeysFromService().createOperationResourceKey();

        return new DictionaryGrantKeys(viewGroupResourceKey, createKey);
    }

    private DictionaryGrantKeys requireDictionaryGrantKeysFromService() {
        CatalogNodeVo module = catalogService.permissionCatalog().stream()
                .filter(node -> DICTIONARY_MODULE_RESOURCE_KEY.equals(node.resourceKey()))
                .findFirst()
                .orElseThrow();
        CatalogPermissionGroupVo viewGroup = module.grants().viewGroups().getFirst();
        String createKey = module.grants().operationGroups().stream()
                .flatMap(group -> group.items().stream())
                .filter(item -> "dictionary.type.create".equals(item.grantName()))
                .map(CatalogViewItemVo::resourceKey)
                .findFirst()
                .orElseThrow();
        return new DictionaryGrantKeys(viewGroup.resourceKey(), createKey);
    }

    private void assertDictionaryViewGroupInCatalogService(DictionaryGrantKeys dictionary) {
        CatalogPermissionGroupVo viewGroup = catalogService.permissionCatalog().stream()
                .filter(node -> DICTIONARY_MODULE_RESOURCE_KEY.equals(node.resourceKey()))
                .findFirst()
                .orElseThrow()
                .grants()
                .viewGroups()
                .getFirst();
        assertThat(viewGroup.resourceKey()).isEqualTo(dictionary.viewGroupResourceKey());
        assertThat(viewGroup.group()).isEqualTo("default");
    }

    private void assertPageAccess(DictionaryGrantKeys dictionary, boolean allowed) throws Exception {
        String body = """
                {
                  "domainKey": "%s",
                  "moduleKey": "dictionary",
                  "pageKey": "%s",
                  "grantGroup": "default"
                }
                """.formatted(BuiltinPlatformEntryPlugins.DOMAIN_KEY, DICTIONARY_PAGE_KEY);
        HttpResponse<String> response = http.exchange(
                "POST",
                PAGE_ACCESS_VERIFY_PATH,
                jsonHeaders(),
                body);
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        JsonNode data = Jsons.mapper().readTree(response.body()).path("data");
        assertThat(data.path("allowed").asBoolean()).isEqualTo(allowed);
        LOGGER.info("page-access verify allowed={} body={}", allowed, response.body());
    }

    private void assertDictionaryCreateForbidden() throws Exception {
        HttpResponse<String> response = http.exchange(
                "POST",
                PlatformConstant.DICTIONARY_TYPES_PATH,
                jsonHeaders(),
                dictionaryCreateBody("denied-type-" + System.nanoTime()));
        assertThat(response.statusCode()).as(response.body()).isEqualTo(403);
    }

    private void assertDictionaryCreateSucceeded() throws Exception {
        HttpResponse<String> response = http.exchange(
                "POST",
                PlatformConstant.DICTIONARY_TYPES_PATH,
                jsonHeaders(),
                dictionaryCreateBody("granted-type-" + System.nanoTime()));
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        assertThat(response.body()).contains("\"success\":true");
    }

    private static String dictionaryCreateBody(String typeCode) {
        return """
                {
                  "typeCode": "%s",
                  "typeName": "Grant flow dictionary",
                  "description": "permission grant flow IT"
                }
                """.formatted(typeCode);
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
        assertThat(login.statusCode()).as("public login status: " + login.body()).isEqualTo(200);
        String token = Jsons.mapper().readTree(login.body()).path("data").path("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private String createRole(String roleCode, String roleName) throws Exception {
        String createBody = """
                {
                  "roleName": "%s",
                  "roleCode": "%s",
                  "description": "permission grant flow IT"
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

    private record DictionaryGrantKeys(String viewGroupResourceKey, String createOperationResourceKey) {
    }
}
